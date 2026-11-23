import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

/**
 * Fairy Garden sound generator. Every sound effect, ambience loop and the music track is synthesised from
 * scratch (no samples, no libraries) and written as a 16-bit mono 44.1 kHz WAV file.
 *
 * <pre>
 *   java tools/audiogen/AudioGenerator.java build/audio
 *   java tools/audiogen/AudioGenerator.java build/audio --ogg src/main/resources/assets/fairygarden/sounds
 * </pre>
 *
 * With {@code --ogg <soundsDir>} every WAV is encoded to mono Ogg Vorbis with ffmpeg (the FFMPEG environment
 * variable, else ffmpeg from PATH) at the matching path below the sounds folder, and the temporary WAVs are
 * deleted afterwards. Each sound draws from its own fixed-seed Random, so every run produces the same audio.
 *
 * Ambience loops are rendered circularly so the wrap point is seamless by construction: every tail that runs
 * past the end of the loop is folded back into its head, oscillators and LFOs complete a whole number of cycles
 * per loop, and noise filters and the reverb are run twice around the loop so their state wraps as well.
 */
public final class AudioGenerator {

    // ================================================================ constants & small helpers

    static final int RATE = 44100;
    static final double TWO_PI = 2 * Math.PI;
    static final long SEED = 0x46414952L; // "FAIR"

    static int samples(double sec) {
        return (int) Math.round(sec * RATE);
    }

    static double[] buffer(double sec) {
        return new double[samples(sec)];
    }

    /** A fixed-seed generator per sound, so changing one sound never changes another. */
    static Random rng(String name) {
        return new Random(SEED * 1_000_003L + name.hashCode());
    }

    static double rand(Random r, double lo, double hi) {
        return lo + (hi - lo) * r.nextDouble();
    }

    static double expRand(Random r, double lo, double hi) {
        return lo * Math.pow(hi / lo, r.nextDouble());
    }

    static double clamp(double x, double lo, double hi) {
        return Math.max(lo, Math.min(hi, x));
    }

    static double smooth(double x) {
        x = clamp(x, 0, 1);
        return x * x * (3 - 2 * x);
    }

    /** Raised-cosine ramp: 0 at x<=0, 1 at x>=1. */
    static double riseCos(double x) {
        return 0.5 - 0.5 * Math.cos(Math.PI * clamp(x, 0, 1));
    }

    /** Quadratic Bezier through a start, a control and an end value. */
    static double bez(double a, double m, double b, double u) {
        return (1 - u) * (1 - u) * a + 2 * u * (1 - u) * m + u * u * b;
    }

    static double cents(double c) {
        return Math.pow(2, c / 1200);
    }

    static double dbToGain(double db) {
        return Math.pow(10, db / 20);
    }

    static double gainToDb(double g) {
        return 20 * Math.log10(Math.max(g, 1e-12));
    }

    static double midiHz(double m) {
        return 440 * Math.pow(2, (m - 69) / 12);
    }

    static final String[] NAMES = {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"};

    static int pitchClass(String pc) {
        for (int i = 0; i < NAMES.length; i++) if (NAMES[i].equals(pc)) return i;
        throw new IllegalArgumentException(pc);
    }

    static int midi(String note) {
        int octave = note.charAt(note.length() - 1) - '0';
        return 12 * (octave + 1) + pitchClass(note.substring(0, note.length() - 1));
    }

    static double hz(String note) {
        return midiHz(midi(note));
    }

    /** Major-pentatonic notes (degrees 1 2 3 5 6 of {@code tonic}) between two MIDI notes. */
    static int[] pentatonic(int lo, int hi, int tonic) {
        List<Integer> out = new ArrayList<>();
        for (int m = lo; m <= hi; m++) {
            int d = Math.floorMod(m - tonic, 12);
            if (d == 0 || d == 2 || d == 4 || d == 7 || d == 9) out.add(m);
        }
        return out.stream().mapToInt(Integer::intValue).toArray();
    }

    /** Nudges a frequency so it completes a whole number of cycles in a loop of the given length. */
    static double loopHz(double f, double sec) {
        return Math.max(1, Math.round(f * sec)) / sec;
    }

    /** Adds v at index i; one-shots ignore samples outside the buffer, loops wrap them around. */
    static void put(double[] b, int i, double v, boolean wrap) {
        if (wrap) b[Math.floorMod(i, b.length)] += v;
        else if (i >= 0 && i < b.length) b[i] += v;
    }

    static void addWrapped(double[] dst, double[] ev, int at) {
        for (int j = 0; j < ev.length; j++) dst[Math.floorMod(at + j, dst.length)] += ev[j];
    }

    static double[] sum(double[]... parts) {
        double[] out = new double[parts[0].length];
        for (double[] p : parts) for (int i = 0; i < out.length; i++) out[i] += p[i];
        return out;
    }

    // ================================================================ filters, noise, reverb

    /** Zero-delay-feedback state-variable filter (stable under fast modulation). */
    static final class Svf {
        double ic1, ic2, lp, bp, bpn, hp;

        void run(double x, double fc, double q) {
            double g = Math.tan(Math.PI * clamp(fc, 5, RATE * 0.45) / RATE), k = 1 / q;
            double a1 = 1 / (1 + g * (g + k)), a2 = g * a1, a3 = g * a2;
            double v3 = x - ic2, v1 = a1 * ic1 + a2 * v3, v2 = ic2 + a2 * ic1 + a3 * v3;
            ic1 = 2 * v1 - ic1;
            ic2 = 2 * v2 - ic2;
            lp = v2;
            bp = v1;
            bpn = k * v1; // unity gain at the centre frequency
            hp = x - k * v1 - v2;
        }
    }

    /** Paul Kellet's pink-noise filter, fed with an external white source. */
    static final class Pink {
        double b0, b1, b2, b3, b4, b5, b6;

        double next(double w) {
            b0 = 0.99886 * b0 + w * 0.0555179;
            b1 = 0.99332 * b1 + w * 0.0750759;
            b2 = 0.96900 * b2 + w * 0.1538520;
            b3 = 0.86650 * b3 + w * 0.3104856;
            b4 = 0.55000 * b4 + w * 0.5329522;
            b5 = -0.7616 * b5 - w * 0.0168980;
            double out = b0 + b1 + b2 + b3 + b4 + b5 + b6 + w * 0.5362;
            b6 = w * 0.115926;
            return out * 0.11;
        }
    }

    /** One-pole lowpass cascade; with {@code loop} the filter is warmed up once around the loop first. */
    static void lowpass(double[] b, double fc, int poles, boolean loop) {
        double a = 1 - Math.exp(-TWO_PI * fc / RATE);
        for (int p = 0; p < poles; p++) {
            double y = 0;
            if (loop) for (double v : b) y += a * (v - y);
            for (int i = 0; i < b.length; i++) {
                y += a * (b[i] - y);
                b[i] = y;
            }
        }
    }

    static void highpass(double[] b, double fc, boolean loop) {
        double a = 1 - Math.exp(-TWO_PI * fc / RATE), lp = 0;
        if (loop) for (double v : b) lp += a * (v - lp);
        for (int i = 0; i < b.length; i++) {
            lp += a * (b[i] - lp);
            b[i] -= lp;
        }
    }

    /**
     * Filtered noise for a loop of n samples: the white source repeats every n samples and the filter runs twice
     * around it (only the second lap is kept), so the noise is exactly periodic. fc and gain are functions of the
     * loop position x in [0,1). mode: 'l' lowpass, 'b' bandpass.
     */
    static double[] loopNoise(Random r, int n, boolean pink, char mode, DoubleUnaryOperator fc, double q,
                              DoubleUnaryOperator gain) {
        double[] white = new double[n];
        for (int i = 0; i < n; i++) white[i] = r.nextDouble() * 2 - 1;
        Pink p = new Pink();
        Svf f = new Svf();
        double[] out = new double[n];
        for (int m = 0; m < 2 * n; m++) {
            int i = m % n;
            double x = i / (double) n;
            f.run(pink ? p.next(white[i]) : white[i], fc.applyAsDouble(x), q);
            if (m >= n) out[i] = (mode == 'l' ? f.lp : f.bpn) * gain.applyAsDouble(x);
        }
        return out;
    }

    /** A slow, smooth swell in [0,1] that repeats exactly once per loop. */
    static DoubleUnaryOperator swell(Random r) {
        double p0 = r.nextDouble() * TWO_PI, p1 = r.nextDouble() * TWO_PI, p2 = r.nextDouble() * TWO_PI;
        return x -> 0.5 + 0.5 * (0.6 * Math.sin(TWO_PI * x + p0) + 0.28 * Math.sin(2 * TWO_PI * x + p1)
                + 0.12 * Math.sin(3 * TWO_PI * x + p2));
    }

    /** A sine LFO between lo and hi with a whole number of cycles per loop. */
    static DoubleUnaryOperator lfo(Random r, int cycles, double lo, double hi) {
        double p = r.nextDouble() * TWO_PI;
        return x -> lo + (hi - lo) * (0.5 + 0.5 * Math.sin(cycles * TWO_PI * x + p));
    }

    /** Fast shimmer (tremolo) under a slow swell, both whole cycles per loop. */
    static DoubleUnaryOperator tremolo(Random r, int fastCycles, int slowCycles) {
        double p = r.nextDouble() * TWO_PI, q = r.nextDouble() * TWO_PI;
        return x -> (0.5 + 0.5 * Math.sin(fastCycles * TWO_PI * x + p)) * (0.5 + 0.5 * Math.sin(slowCycles * TWO_PI * x + q));
    }

    /** Mono Freeverb-style reverb: 8 damped combs into 4 allpasses, mixed onto the dry signal. */
    static double[] reverb(double[] in, double mix, double feedback, double damp) {
        int[] combs = {1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617};
        int[] alls = {556, 441, 341, 225};
        double[][] cb = new double[combs.length][];
        double[][] ab = new double[alls.length][];
        int[] ci = new int[combs.length], ai = new int[alls.length];
        double[] store = new double[combs.length];
        for (int k = 0; k < combs.length; k++) cb[k] = new double[combs[k]];
        for (int k = 0; k < alls.length; k++) ab[k] = new double[alls[k]];
        double[] out = new double[in.length];
        for (int i = 0; i < in.length; i++) {
            double x = in[i] * 0.015, acc = 0;
            for (int k = 0; k < combs.length; k++) {
                double y = cb[k][ci[k]];
                store[k] = y * (1 - damp) + store[k] * damp;
                cb[k][ci[k]] = x + store[k] * feedback;
                if (++ci[k] == cb[k].length) ci[k] = 0;
                acc += y;
            }
            for (int k = 0; k < alls.length; k++) {
                double bo = ab[k][ai[k]];
                double y = bo - acc;
                ab[k][ai[k]] = acc + bo * 0.5;
                if (++ai[k] == ab[k].length) ai[k] = 0;
                acc = y;
            }
            out[i] = in[i] + mix * 3 * acc;
        }
        return out;
    }

    /** Reverb for a loop: process two laps and keep the second, so the tail of the end rings into the start. */
    static double[] reverbLoop(double[] loop, double mix, double feedback, double damp) {
        int n = loop.length;
        double[] two = new double[2 * n];
        System.arraycopy(loop, 0, two, 0, n);
        System.arraycopy(loop, 0, two, n, n);
        return Arrays.copyOfRange(reverb(two, mix, feedback, damp), n, 2 * n);
    }

    // ================================================================ levels

    static double peak(double[] b) {
        double p = 1e-12;
        for (double v : b) p = Math.max(p, Math.abs(v));
        return p;
    }

    static double rms(double[] b) {
        double s = 0;
        for (double v : b) s += v * v;
        return Math.sqrt(s / b.length) + 1e-12;
    }

    static void gain(double[] b, double g) {
        for (int i = 0; i < b.length; i++) b[i] *= g;
    }

    static void scaleRms(double[] b, double target) {
        gain(b, target / rms(b));
    }

    static void normalizePeak(double[] b, double db) {
        gain(b, dbToGain(db) / peak(b));
    }

    /** Sets the RMS level, backing off if that would push the peak above the cap. */
    static void normalizeRms(double[] b, double rmsDb, double peakCapDb) {
        double g = dbToGain(rmsDb) / rms(b);
        if (peak(b) * g > dbToGain(peakCapDb)) g = dbToGain(peakCapDb) / peak(b);
        gain(b, g);
    }

    /**
     * Rotates a loop (a circular shift, so it stays seamless) to start in its quietest 0.4 s, on an upward zero
     * crossing, so the first play-through also begins softly and without a jump from silence.
     */
    static double[] quietStart(double[] b) {
        int n = b.length, win = samples(0.4), hop = samples(0.005);
        double[] pre = new double[2 * n + 1];
        for (int i = 0; i < 2 * n; i++) pre[i + 1] = pre[i] + b[i % n] * b[i % n];
        int at = 0;
        double best = Double.MAX_VALUE;
        for (int s = 0; s < n; s += hop) {
            double e = pre[s + win] - pre[s];
            if (e < best) {
                best = e;
                at = s;
            }
        }
        int cross = at;
        double small = Double.MAX_VALUE;
        for (int i = at; i < at + samples(0.05); i++) {
            double prev = b[Math.floorMod(i - 1, n)], cur = b[i % n];
            if (prev <= 0 && cur > 0 && cur < small) {
                small = cur;
                cross = i % n;
            }
        }
        double[] out = new double[n];
        for (int j = 0; j < n; j++) out[j] = b[(cross + j) % n];
        return out;
    }

    /** Raised-cosine fade at both ends of a one-shot (the last sample is exactly zero). */
    static void fades(double[] b, double inSec, double outSec) {
        int n = b.length, fi = Math.max(1, samples(inSec)), fo = Math.max(1, samples(outSec));
        for (int i = 0; i < fi && i < n; i++) b[i] *= riseCos(i / (double) fi);
        for (int i = 0; i < fo && i < n; i++) b[n - 1 - i] *= riseCos(i / (double) fo);
    }

    // ================================================================ voices

    /** Partials of a struck or plucked voice: frequency ratios, amplitudes and decay-time multipliers. */
    record Partials(double[] ratio, double[] amp, double[] decay) {
    }

    /** Small glockenspiel bar: free-bar partials plus a detuned twin of the fundamental for shimmer. */
    static final Partials GLOCK = new Partials(
            new double[]{1, 1.0017, 2.756, 5.404, 8.933},
            new double[]{1, 0.42, 0.26, 0.08, 0.03},
            new double[]{1, 0.92, 0.42, 0.22, 0.12});
    /** Glassy crystal: inharmonic, sweet, beating fundamental pair. */
    static final Partials CRYSTAL = new Partials(
            new double[]{1, 1.0024, 2.405, 3.981, 6.152},
            new double[]{1, 0.5, 0.3, 0.11, 0.045},
            new double[]{1, 0.85, 0.5, 0.3, 0.17});
    /** Wind-chime tube. */
    static final Partials TUBE = new Partials(
            new double[]{1, 1.0011, 2.756, 5.404},
            new double[]{1, 0.3, 0.32, 0.1},
            new double[]{1, 0.95, 0.5, 0.25});
    /** Tiny sparkle ping. */
    static final Partials PING = new Partials(
            new double[]{1, 2.756},
            new double[]{1, 0.2},
            new double[]{1, 0.45});
    static final Partials MUSIC_BOX = new Partials(
            new double[]{1, 1.0008, 2.002, 4.07, 6.27},
            new double[]{1, 0.3, 0.28, 0.06, 0.03},
            new double[]{1, 1, 0.4, 0.16, 0.09});
    static final Partials CELESTA = new Partials(
            new double[]{1, 1.0012, 2.0, 4.0},
            new double[]{1, 0.35, 0.1, 0.08},
            new double[]{1, 0.9, 0.5, 0.18});
    /** Plucked harp string, very slightly stretched. */
    static final Partials HARP = new Partials(
            new double[]{1, 2.0006, 3.0018, 4.0036, 5.006, 6.009, 7.013},
            new double[]{1, 0.45, 0.22, 0.12, 0.065, 0.04, 0.025},
            new double[]{1, 0.62, 0.42, 0.3, 0.22, 0.17, 0.13});
    static final Partials BASS = new Partials(
            new double[]{1, 2},
            new double[]{1, 0.12},
            new double[]{1, 0.5});

    /**
     * Adds one strike: each partial is a sine (recursive oscillator) with a raised-cosine attack and an exponential
     * decay of time constant tau * decay[k]. Partials above 4.5 kHz are progressively softened and anything above
     * 11 kHz is dropped, which keeps the high register sweet rather than piercing.
     */
    static void strike(double[] b, double start, double f, double amp, double tau, Partials p, double attack,
                       boolean wrap) {
        int s0 = samples(start), na = Math.max(1, samples(attack));
        for (int k = 0; k < p.ratio().length; k++) {
            double fk = f * p.ratio()[k];
            if (fk > 11000) continue;
            double ak = amp * p.amp()[k] * (fk > 4500 ? Math.pow(4500 / fk, 1.5) : 1);
            double tk = tau * p.decay()[k];
            int n = samples(tk * 8) + na;
            if (!wrap) n = Math.min(n, b.length - s0);
            if (n <= 0) continue;
            int taper = Math.max(1, Math.min(n / 4, samples(0.01)));
            double w = TWO_PI * fk / RATE, c2 = 2 * Math.cos(w), s = 0, sPrev = -Math.sin(w);
            double env = 1, d = Math.exp(-1.0 / (tk * RATE));
            for (int j = 0; j < n; j++) {
                double a = j < na ? 0.5 - 0.5 * Math.cos(Math.PI * j / na) : 1;
                double tp = j > n - taper ? (n - j) / (double) taper : 1;
                put(b, s0 + j, ak * a * tp * env * s, wrap);
                double nx = c2 * s - sPrev;
                sPrev = s;
                s = nx;
                env *= d;
            }
        }
    }

    /**
     * A bird syllable: a sine whose pitch follows a Bezier curve f0 -> fm -> f1, with optional vibrato, a fast
     * raised-cosine attack/release and a body that sags by {@code sag} (0 = flat).
     */
    static void chirp(double[] b, double start, double dur, double f0, double fm, double f1,
                      double vibHz, double vibDepth, double amp, double sag) {
        int s0 = samples(start), n = samples(dur);
        double att = Math.min(0.007, dur * 0.25), rel = Math.min(0.06, dur * 0.4), ph = 0;
        for (int j = 0; j < n; j++) {
            double t = j / (double) RATE, u = j / (double) n;
            double f = bez(f0, fm, f1, u) * (1 + vibDepth * Math.sin(TWO_PI * vibHz * t));
            ph += TWO_PI * f / RATE;
            double env = riseCos(t / att) * riseCos((dur - t) / rel) * Math.pow(1 - 0.85 * u, sag);
            put(b, s0 + j, amp * env * (Math.sin(ph) + 0.07 * Math.sin(2 * ph)), false);
        }
    }

    /**
     * A breathy hummed sine gliding through (time, frequency) breakpoints, with delayed vibrato, soft amplitude
     * dips at the given times (little syllables) and band-passed noise that follows the pitch.
     */
    static void hum(double[] b, Random r, double start, double[] times, double[] freqs, double[] dips,
                    double vibHz, double vibDepth, double amp, double breath) {
        double dur = times[times.length - 1];
        int s0 = samples(start), n = samples(dur), seg = 0;
        Svf bp = new Svf();
        double ph = 0, att = 0.09, rel = Math.min(0.3, dur * 0.4);
        for (int j = 0; j < n; j++) {
            double t = j / (double) RATE;
            while (seg < times.length - 2 && t > times[seg + 1]) seg++;
            double u = (t - times[seg]) / (times[seg + 1] - times[seg]);
            double f = freqs[seg] + (freqs[seg + 1] - freqs[seg]) * smooth(u);
            f *= 1 + vibDepth * riseCos((t - 0.1) / 0.25) * Math.sin(TWO_PI * vibHz * t);
            ph += TWO_PI * f / RATE;
            double env = riseCos(t / att) * riseCos((dur - t) / rel);
            for (double d : dips) env *= 1 - 0.4 * Math.exp(-Math.pow((t - d) / 0.035, 2));
            bp.run(r.nextDouble() * 2 - 1, f, 3.0);
            double v = Math.sin(ph) + 0.1 * Math.sin(2 * ph) + 0.03 * Math.sin(3 * ph) + breath * 1.6 * bp.bpn;
            put(b, s0 + j, amp * env * v, false);
        }
    }

    /** A cricket pulse: a sine burst under a sin^2 window (always wraps: crickets only live in loops). */
    static void pulse(double[] b, double start, double len, double f, double amp) {
        int s0 = samples(start), n = samples(len);
        double w = TWO_PI * f / RATE;
        for (int j = 0; j < n; j++) {
            double win = Math.sin(Math.PI * j / n);
            put(b, s0 + j, amp * win * win * Math.sin(w * j), true);
        }
    }

    /** A sustained pad voice for a loop: three detuned sines plus a soft octave, all whole cycles per loop. */
    static void padVoice(double[] b, double f, double amp, DoubleUnaryOperator weight, Random r) {
        int n = b.length;
        double sec = n / (double) RATE;
        double[] det = {0, 4.5, -4.5}, mix = {1, 0.7, 0.7}, w = new double[6], ph = new double[6];
        for (int v = 0; v < 3; v++) {
            w[v] = TWO_PI * loopHz(f * cents(det[v]), sec) / RATE;
            w[v + 3] = TWO_PI * loopHz(2 * f * cents(det[v]), sec) / RATE;
            ph[v] = r.nextDouble() * TWO_PI;
            ph[v + 3] = r.nextDouble() * TWO_PI;
        }
        for (int i = 0; i < n; i++) {
            double s = 0;
            for (int v = 0; v < 3; v++) s += mix[v] * (Math.sin(w[v] * i + ph[v]) + 0.1 * Math.sin(w[v + 3] * i + ph[v + 3]));
            b[i] += amp * weight.applyAsDouble(i / (double) n) * s / 2.4;
        }
    }

    /** A bee flying past: band-limited sawtooth buzz with doppler bend, jitter, proximity swell and lowpass. */
    static double[] beeBuzz(Random r, double sec, double f0, double tcFrac, double width, double doppler,
                            double fcFar, double fcNear, double hover) {
        int n = samples(sec);
        double[] out = new double[n];
        double tc = sec * tcFrac, ph1 = 0, ph2 = 0.37, jit = 0, target = 0;
        Svf lp1 = new Svf(), lp2 = new Svf();
        for (int j = 0; j < n; j++) {
            double t = j / (double) RATE, x = (t - tc) / width;
            double prox = 1 / (1 + x * x), approach = -x / Math.sqrt(1 + x * x);
            if (j % 441 == 0) target = r.nextDouble() * 2 - 1;
            jit += (target - jit) * 0.0009;
            double f = f0 * (1 + doppler * approach + 0.012 * jit + hover * Math.sin(TWO_PI * 1.4 * t));
            double dt1 = f / RATE, dt2 = f * 1.006 / RATE;
            ph1 += dt1;
            if (ph1 >= 1) ph1 -= 1;
            ph2 += dt2;
            if (ph2 >= 1) ph2 -= 1;
            double saw = (2 * ph1 - 1 - polyBlep(ph1, dt1)) + 0.5 * (2 * ph2 - 1 - polyBlep(ph2, dt2));
            double fc = fcFar + (fcNear - fcFar) * prox;
            lp1.run(saw, fc, 0.8);
            lp2.run(lp1.lp, fc * 1.3, 0.7);
            out[j] = lp2.lp * Math.pow(prox, 1.3) * (1 + 0.12 * jit);
        }
        fades(out, 0.25, 0.35);
        return out;
    }

    static double polyBlep(double t, double dt) {
        if (t < dt) {
            double x = t / dt;
            return x + x - x * x - 1;
        }
        if (t > 1 - dt) {
            double x = (t - 1) / dt;
            return x * x + x + x + 1;
        }
        return 0;
    }

    // ================================================================ ambient: songbirds

    static double[] finishBird(double[] b) {
        lowpass(b, 7000, 2, false);
        b = reverb(b, 0.12, 0.72, 0.5);
        normalizePeak(b, -6);
        fades(b, 0.003, 0.08);
        return b;
    }

    /** "tsee-tsee-tsee" then a little trill and a closing down-slur. */
    static double[] birds1() {
        double[] b = buffer(2.3);
        for (int k = 0; k < 3; k++) chirp(b, 0.04 + 0.21 * k, 0.085, 2600, 3000, 3950, 0, 0, 0.8 + 0.1 * k, 0.6);
        for (int k = 0; k < 12; k++) {
            double f = 3650 - 45 * k;
            chirp(b, 0.74 + 0.056 * k, 0.042, f + 380, f + 150, f - 350, 40, 0.01, 0.85 - 0.025 * k, 0.8);
        }
        chirp(b, 1.52, 0.26, 3900, 3300, 2500, 0, 0, 0.8, 0.9);
        return finishBird(b);
    }

    /** A warbled, robin-like phrase of curved syllables. */
    static double[] birds2() {
        double[] b = buffer(2.4);
        double[][] syl = {{2300, 3300, 2600}, {2800, 2400, 3100}, {2500, 3500, 2900}, {3000, 2600, 2300}};
        double t = 0.05;
        for (double[] s : syl) {
            chirp(b, t, 0.17, s[0], s[1], s[2], 0, 0, 0.9, 0.4);
            t += 0.26;
        }
        t += 0.3;
        chirp(b, t, 0.2, 2600, 3700, 2800, 38, 0.025, 1.0, 0.4);
        t += 0.29;
        chirp(b, t, 0.18, 3100, 2650, 2950, 0, 0, 0.85, 0.5);
        return finishBird(b);
    }

    /** A busy little warbler: a seeded random walk of quick syllables. */
    static double[] birds3() {
        Random r = rng("birds3");
        double[] b = buffer(2.8);
        double t = 0.04, centre = 3300;
        for (int k = 0; k < 17; k++) {
            centre = clamp(centre + rand(r, -500, 500), 2500, 4300);
            double dur = rand(r, 0.045, 0.1);
            boolean vib = r.nextDouble() < 0.3;
            chirp(b, t, dur, centre + rand(r, -400, 400), centre + rand(r, -700, 700), centre + rand(r, -500, 500),
                    vib ? 32 : 0, vib ? 0.035 : 0, rand(r, 0.6, 1.0), 0.7);
            t += dur + rand(r, 0.025, 0.08);
        }
        return finishBird(b);
    }

    /** A clear two-note "fee-bee" whistle and two soft chips. */
    static double[] birds4() {
        double[] b = buffer(1.9);
        chirp(b, 0.05, 0.4, 3950, 3920, 3860, 5.5, 0.004, 1.0, 0.25);
        chirp(b, 0.52, 0.22, 3420, 3380, 3330, 0, 0, 0.85, 0.2);
        chirp(b, 0.71, 0.26, 3340, 3320, 3280, 0, 0, 0.8, 0.5);
        chirp(b, 1.28, 0.035, 4300, 3800, 3000, 0, 0, 0.45, 1.0);
        chirp(b, 1.40, 0.035, 4200, 3700, 2900, 0, 0, 0.4, 1.0);
        return finishBird(b);
    }

    // ================================================================ ambient: lavender fields

    /** Warm summer breeze in slow swells, rustling gusts, and a faint bee drifting by now and then. */
    static double[] lavenderLoop() {
        Random r = rng("lavender_loop");
        int n = samples(24);
        DoubleUnaryOperator s1 = swell(r), s2 = swell(r);
        double[] breeze = loopNoise(r, n, true, 'l', x -> 330 + 600 * s1.applyAsDouble(x), 0.62,
                x -> 0.35 + 0.65 * s1.applyAsDouble(x));
        highpass(breeze, 130, true); // keep it airy rather than rumbly
        highpass(breeze, 130, true);
        double[] gust = loopNoise(r, n, false, 'b', x -> 850 + 950 * s2.applyAsDouble(x), 0.9,
                x -> Math.pow(s2.applyAsDouble(x), 2));
        double[] leaves = loopNoise(r, n, false, 'b', x -> 3200 + 600 * s1.applyAsDouble(x), 0.8,
                x -> 0.3 + 0.7 * s2.applyAsDouble(x));
        double[] bees = new double[n];
        addWrapped(bees, beeBuzz(r, 6.5, 210, 0.5, 1.4, 0.015, 450, 850, 0.01), samples(6.0));
        addWrapped(bees, beeBuzz(r, 7.0, 232, 0.5, 1.6, 0.012, 430, 820, 0.012), samples(18.5)); // wraps the seam
        scaleRms(breeze, 1.0);
        scaleRms(gust, 0.33);
        scaleRms(leaves, 0.05);
        scaleRms(bees, 0.12);
        double[] out = sum(breeze, gust, leaves, bees);
        highpass(out, 60, true);
        normalizeRms(out, -18, -4);
        return quietStart(out);
    }

    static double[] bees(String name, double sec, double f0, double tcFrac, double width, double doppler, double hover) {
        Random r = rng(name);
        double[] b = beeBuzz(r, sec, f0, tcFrac, width, doppler, 650, 2100, hover);
        normalizePeak(b, -8);
        fades(b, 0.01, 0.05);
        return b;
    }

    // ================================================================ ambient: moonlit forest

    /** Night forest: three kinds of crickets over a very quiet, slowly breathing Dm9 / Bbmaj9 pad. */
    static double[] moonlightLoop() {
        Random r = rng("moonlight_loop");
        double sec = 24;
        int n = samples(sec);
        double[] pad = new double[n];
        padVoice(pad, hz("D3"), 0.45, x -> 0.5 + 0.5 * Math.cos(TWO_PI * x), r);
        padVoice(pad, hz("Bb2"), 0.45, x -> 0.5 - 0.5 * Math.cos(TWO_PI * x), r);
        padVoice(pad, hz("F3"), 0.45, lfo(r, 1, 0.5, 1), r);
        padVoice(pad, hz("A3"), 0.5, lfo(r, 2, 0.45, 1), r);
        padVoice(pad, hz("E4"), 0.36, lfo(r, 3, 0.3, 1), r);
        padVoice(pad, hz("C5"), 0.22, lfo(r, 2, 0.2, 1), r);
        padVoice(pad, hz("E6"), 0.05, tremolo(r, 132, 3), r);
        padVoice(pad, hz("A6"), 0.035, tremolo(r, 150, 2), r);
        scaleRms(pad, 0.035);
        double p = r.nextDouble() * TWO_PI;
        double[] air = loopNoise(r, n, false, 'b', x -> 2600 + 500 * Math.sin(2 * TWO_PI * x + p), 1.0, lfo(r, 3, 0.3, 1));
        scaleRms(air, 0.014);

        double[] crickets = new double[n];
        int chirpsA = 40; // field cricket: three-pulse chirps, a whole number per loop
        for (int c = 0; c < chirpsA; c++) {
            double t0 = 0.1 + c * sec / chirpsA + rand(r, -0.008, 0.008);
            double amp = 0.12 * (0.75 + 0.25 * Math.cos(TWO_PI * t0 / sec)) * rand(r, 0.85, 1.0);
            double fc = 4400 * cents(rand(r, -6, 6));
            for (int k = 0; k < 3; k++) pulse(crickets, t0 + k * 0.034, 0.017, fc, amp * (k == 2 ? 0.8 : 1));
        }
        int chirpsB = 34; // a smaller, farther cricket with four-pulse chirps
        for (int c = 0; c < chirpsB; c++) {
            double t0 = 0.33 + c * sec / chirpsB + rand(r, -0.01, 0.01);
            double amp = 0.065 * (0.6 + 0.4 * Math.sin(2 * TWO_PI * t0 / sec + 1.0)) * rand(r, 0.8, 1.0);
            double fc = 4950 * cents(rand(r, -8, 8));
            for (int k = 0; k < 4; k++) pulse(crickets, t0 + k * 0.026, 0.013, fc, amp);
        }
        for (int burst = 0; burst < 6; burst++) { // distant tree cricket: soft trills every 4 s
            double b0 = 1.2 + burst * 4.0, len = 1.8;
            for (double t = 0; t < len; t += 0.04)
                pulse(crickets, b0 + t, 0.03, 3650, 0.035 * riseCos(t / 0.35) * riseCos((len - t) / 0.35));
        }
        lowpass(crickets, 6500, 1, true);

        double[] out = reverbLoop(sum(pad, air, crickets), 0.3, 0.86, 0.45);
        highpass(out, 40, true);
        normalizeRms(out, -20, -6);
        return quietStart(out);
    }

    /** Two-note "hoo-hoo": a low sine with a breathy onset; the second hoot wavers. */
    static double[] owl() {
        Random r = rng("night1");
        double[] b = buffer(3.2);
        hoot(b, r, 0.10, 0.46, 392, 412, 380, 0, 1.0);
        hoot(b, r, 0.85, 0.78, 384, 404, 352, 6.5, 0.9);
        lowpass(b, 1400, 2, false);
        b = reverb(b, 0.35, 0.86, 0.5);
        normalizePeak(b, -6);
        fades(b, 0.005, 0.5);
        return b;
    }

    static void hoot(double[] b, Random r, double start, double dur, double f0, double fm, double f1, double waver, double amp) {
        int s0 = samples(start), n = samples(dur);
        Svf br = new Svf();
        double ph = 0;
        for (int j = 0; j < n; j++) {
            double t = j / (double) RATE, u = j / (double) n;
            double f = bez(f0, fm, f1, u) * (1 + 0.004 * Math.sin(TWO_PI * waver * t));
            ph += TWO_PI * f / RATE;
            double env = riseCos(t / 0.07) * riseCos((dur - t) / 0.16) * (1 - 0.25 * u);
            if (waver > 0) env *= 1 - 0.22 * (0.5 + 0.5 * Math.cos(TWO_PI * waver * t)) * smooth(u * 3);
            double tone = Math.sin(ph) + 0.10 * Math.sin(2 * ph) + 0.025 * Math.sin(3 * ph);
            br.run(r.nextDouble() * 2 - 1, 700, 0.9);
            double breath = br.bpn * (0.10 + 0.25 * Math.exp(-t / 0.05));
            put(b, s0 + j, amp * env * (tone + breath), false);
        }
    }

    /** A gentle wind-chime brush: a cascade of tubes from a D-major pentatonic set. */
    static double[] windChimes() {
        Random r = rng("night2");
        double[] b = buffer(3.6);
        String[] set = {"A5", "B5", "D6", "E6", "F#6", "A6", "B6"};
        int last = -1;
        for (int k = 0; k < 11; k++) {
            double t = 0.04 + 1.25 * Math.pow(k / 10.0, 1.35) + rand(r, -0.02, 0.02);
            int idx;
            do idx = r.nextInt(set.length); while (idx == last);
            last = idx;
            double amp = (1 - 0.45 * k / 10.0) * rand(r, 0.6, 1.0);
            strike(b, Math.max(0.01, t), hz(set[idx]) * cents(rand(r, -6, 6)), amp, rand(r, 0.9, 1.4), TUBE, 0.002, false);
        }
        lowpass(b, 8000, 1, false);
        b = reverb(b, 0.28, 0.84, 0.35);
        normalizePeak(b, -6);
        fades(b, 0.003, 0.9);
        return b;
    }

    /** A single magical sweep: pentatonic pings rising over a rising noise swish, then a glittery tail. */
    static double[] sparkleSweep() {
        Random r = rng("night3");
        double[] b = buffer(2.8);
        int[] notes = pentatonic(79, 100, 0); // G5 .. E7 in C major pentatonic
        int last = notes.length - 1;
        for (int k = 0; k <= last; k++) {
            double t = 0.06 + 0.8 * Math.pow(k / (double) last, 0.85);
            strike(b, t, midiHz(notes[k]), 0.5 + 0.3 * k / last, 0.35, GLOCK, 0.0015, false);
        }
        Svf f = new Svf();
        double len = 1.3;
        for (int j = 0; j < samples(len); j++) {
            double t = j / (double) RATE;
            double fc = 600 * Math.pow(5200 / 600.0, clamp(t / 0.95, 0, 1));
            double env = riseCos(t / 0.75) * riseCos((len - t) / 0.5);
            f.run(r.nextDouble() * 2 - 1, fc, 1.4);
            b[j] += 0.35 * env * f.bpn;
        }
        for (int k = 0; k < 14; k++) {
            double t = 0.9 + 1.2 * r.nextDouble();
            strike(b, t, expRand(r, 2400, 4600), 0.18 * (1 - (t - 0.9) / 1.4), 0.12, PING, 0.0015, false);
        }
        lowpass(b, 9000, 1, false);
        b = reverb(b, 0.3, 0.85, 0.35);
        normalizePeak(b, -6);
        fades(b, 0.003, 0.5);
        return b;
    }

    // ================================================================ ambient: fairy glade

    /** Dreamy F-lydian pad swaying between Fmaj9 and G/F, with airy noise and sparse twinkles. */
    static double[] gladeLoop() {
        Random r = rng("glade_loop");
        double sec = 24;
        int n = samples(sec);
        DoubleUnaryOperator a = x -> 0.5 + 0.5 * Math.cos(TWO_PI * x), bChord = x -> 0.5 - 0.5 * Math.cos(TWO_PI * x);
        double[] pad = new double[n];
        padVoice(pad, hz("F3"), 0.6, lfo(r, 1, 0.75, 1), r);
        padVoice(pad, hz("C4"), 0.45, a, r);
        padVoice(pad, hz("E4"), 0.38, a, r);
        padVoice(pad, hz("A4"), 0.34, a, r);
        padVoice(pad, hz("G4"), 0.4, lfo(r, 2, 0.6, 1), r);
        padVoice(pad, hz("D4"), 0.42, bChord, r);
        padVoice(pad, hz("B4"), 0.3, bChord, r);
        padVoice(pad, hz("D5"), 0.2, bChord, r);
        padVoice(pad, hz("E5"), 0.14, lfo(r, 3, 0.2, 1), r);
        scaleRms(pad, 0.05);
        double[] air = loopNoise(r, n, false, 'b', lfo(r, 2, 3200, 5200), 0.7, lfo(r, 3, 0.35, 1));
        scaleRms(air, 0.01);
        double[] tw = new double[n];
        int[] notes = {midi("A5"), midi("C6"), midi("D6"), midi("F6"), midi("G6"), midi("A6"), midi("C7")};
        for (int k = 0; k < 10; k++) {
            double t = k * 2.4 + rand(r, 0.2, 1.9);
            int m = notes[r.nextInt(notes.length)];
            double amp = rand(r, 0.05, 0.09);
            strike(tw, t, midiHz(m), amp, rand(r, 0.55, 0.95), GLOCK, 0.002, true);
            if (r.nextDouble() < 0.4)
                strike(tw, t + 0.11, midiHz(m + (r.nextBoolean() ? 5 : 7)), amp * 0.6, 0.6, GLOCK, 0.002, true);
        }
        lowpass(tw, 9000, 1, true);
        double[] out = reverbLoop(sum(pad, air, tw), 0.35, 0.87, 0.35);
        highpass(out, 40, true);
        normalizeRms(out, -20, -6);
        return quietStart(out);
    }

    static double[] chimes(String name, String[] notes, double[] times, double[] amps, double tau, double sec) {
        Random r = rng(name);
        double[] b = buffer(sec);
        for (int k = 0; k < notes.length; k++)
            strike(b, times[k], hz(notes[k]) * cents(rand(r, -4, 4)), amps[k], tau * rand(r, 0.85, 1.15), GLOCK, 0.0018, false);
        lowpass(b, 9000, 1, false);
        b = reverb(b, 0.22, 0.82, 0.35);
        normalizePeak(b, -6);
        fades(b, 0.002, 0.3);
        return b;
    }

    // ================================================================ blocks, items, entities

    /** Glassy crystal chime: a sweet inharmonic strike, a whisper of octave and a soft fifth grace note. */
    static double[] crystalChime(String name, String note) {
        Random r = rng(name);
        double[] b = buffer(1.25);
        double f = hz(note);
        Svf hp = new Svf();
        for (int j = 0; j < samples(0.006); j++) {
            double t = j / (double) RATE;
            hp.run(r.nextDouble() * 2 - 1, 6000, 0.7);
            b[j] += 0.08 * riseCos(t / 0.0008) * Math.exp(-t / 0.0015) * hp.bpn;
        }
        strike(b, 0, f, 0.8, 0.34, CRYSTAL, 0.0015, false);
        strike(b, 0, f * 2, 0.10, 0.22, PING, 0.0015, false);
        strike(b, 0.085, f * 1.5, 0.22, 0.26, CRYSTAL, 0.002, false);
        lowpass(b, 9000, 2, false);
        b = reverb(b, 0.15, 0.8, 0.35);
        normalizePeak(b, -4);
        fades(b, 0.001, 0.2);
        return b;
    }

    /** Crystal shattering: a noise crack plus a burst of many short glassy pings, densest at the start. */
    static double[] crystalBreak(String name, double sec, int shards) {
        Random r = rng(name);
        double[] b = buffer(sec);
        Svf hp = new Svf(), bp = new Svf();
        for (int j = 0; j < samples(0.09); j++) {
            double t = j / (double) RATE, w = r.nextDouble() * 2 - 1;
            hp.run(w, 2400, 0.7);
            bp.run(w, 1100, 1.1);
            b[j] += riseCos(t / 0.0006) * (0.55 * Math.exp(-t / 0.011) * hp.hp + 0.4 * Math.exp(-t / 0.028) * bp.bpn);
        }
        for (int k = 0; k < 2; k++) strike(b, 0.002 + 0.012 * k, expRand(r, 1150, 1600), 0.45, 0.16, CRYSTAL, 0.0008, false);
        for (int k = 0; k < shards; k++) {
            double t = 0.002 + 0.30 * Math.pow(r.nextDouble(), 1.8);
            strike(b, t, expRand(r, 1800, 5400), rand(r, 0.18, 0.5) * (1 - t / 0.5), rand(r, 0.03, 0.11), CRYSTAL, 0.0006, false);
        }
        lowpass(b, 9500, 2, false);
        b = reverb(b, 0.12, 0.78, 0.4);
        normalizePeak(b, -3);
        fades(b, 0.0005, 0.12);
        return b;
    }

    /** A very short, soft glassy tick. */
    static double[] crystalStep(String name, double sec, double f) {
        Random r = rng(name);
        double[] b = buffer(sec);
        Svf bp = new Svf();
        for (int j = 0; j < samples(0.004); j++) {
            double t = j / (double) RATE;
            bp.run(r.nextDouble() * 2 - 1, 4200, 1.0);
            b[j] += 0.35 * riseCos(t / 0.0005) * riseCos((0.004 - t) / 0.002) * bp.bpn;
        }
        strike(b, 0.0005, f, 0.55, 0.032, CRYSTAL, 0.0007, false);
        strike(b, 0.0005, f * 0.5, 0.25, 0.045, PING, 0.001, false);
        lowpass(b, 8000, 2, false);
        normalizePeak(b, -8);
        fades(b, 0.0005, 0.05);
        return b;
    }

    /** A soft rising swish with sparkles that opens into a gentle bell. */
    static double[] moonflowerBloom() {
        Random r = rng("moonflower_bloom");
        double[] b = buffer(1.45);
        Svf f = new Svf();
        Pink p = new Pink();
        double len = 1.15;
        for (int j = 0; j < samples(len); j++) {
            double t = j / (double) RATE;
            double fc = 350 * Math.pow(2800 / 350.0, smooth(t / 0.85));
            f.run(p.next(r.nextDouble() * 2 - 1), fc, 1.3);
            b[j] += 0.5 * riseCos(t / 0.62) * riseCos((len - t) / 0.4) * f.bpn;
        }
        int[] notes = pentatonic(81, 93, 9); // A5 .. A6, A major pentatonic
        int last = notes.length - 1;
        for (int k = 0; k <= last; k++)
            strike(b, 0.14 + 0.62 * k / last, midiHz(notes[k]), 0.18 + 0.12 * k / last, 0.12, PING, 0.0015, false);
        strike(b, 0.82, hz("A5"), 0.75, 0.42, GLOCK, 0.003, false);
        strike(b, 0.845, hz("E6"), 0.35, 0.36, GLOCK, 0.003, false);
        lowpass(b, 9000, 1, false);
        b = reverb(b, 0.25, 0.82, 0.4);
        normalizePeak(b, -5);
        fades(b, 0.003, 0.2);
        return b;
    }

    /** Glitter sprinkle: a fast descending cascade of pings, an upward flick, and airy noise. */
    static double[] fairyDust() {
        Random r = rng("fairy_dust");
        double[] b = buffer(0.85);
        int[] notes = pentatonic(84, 100, 0); // C6 .. E7
        int top = notes.length - 1, count = 14;
        for (int k = 0; k < count; k++) {
            double u = k / (count - 1.0);
            double t = 0.01 + 0.42 * u + rand(r, -0.008, 0.008);
            int idx = (int) clamp(Math.round((1 - u) * top + rand(r, -0.6, 0.6)), 0, top);
            strike(b, Math.max(0.004, t), midiHz(notes[idx]) * cents(rand(r, -8, 8)), 0.6 * rand(r, 0.5, 1) * (1 - 0.4 * u),
                    rand(r, 0.07, 0.16), PING, 0.001, false);
        }
        for (int k = 0; k < 3; k++) strike(b, 0.47 + 0.04 * k, midiHz(notes[2 + 2 * k]), 0.35, 0.12, PING, 0.001, false);
        Svf f = new Svf();
        for (int j = 0; j < b.length; j++) {
            double t = j / (double) RATE;
            f.run(r.nextDouble() * 2 - 1, 5200, 0.7);
            b[j] += 0.4 * riseCos(t / 0.03) * Math.exp(-t / 0.22) * f.bpn;
        }
        lowpass(b, 10000, 1, false);
        b = reverb(b, 0.18, 0.78, 0.4);
        normalizePeak(b, -5);
        fades(b, 0.002, 0.12);
        return b;
    }

    /** Wisp hum: a breathy "hm-hmm?" that glides up. */
    static double[] wisp1() {
        Random r = rng("wisp_ambient1");
        double[] b = buffer(1.6);
        hum(b, r, 0.03, new double[]{0, 0.3, 0.5, 0.85, 1.3}, new double[]{660, 700, 880, 990, 940},
                new double[]{0.5}, 6.0, 0.013, 0.9, 1.0);
        strike(b, 1.12, 2637, 0.08, 0.25, PING, 0.002, false);
        lowpass(b, 6000, 1, false);
        b = reverb(b, 0.25, 0.8, 0.45);
        normalizePeak(b, -6);
        fades(b, 0.004, 0.15);
        return b;
    }

    /** Wisp hum: a little "doo-dee-daa" in two softly detuned voices. */
    static double[] wisp2() {
        Random r = rng("wisp_ambient2");
        double[] b = buffer(1.85);
        for (double det : new double[]{0, 7}) {
            double c = cents(det);
            hum(b, r, 0.03, new double[]{0, 0.38, 0.5, 0.85, 1.0, 1.65},
                    new double[]{784 * c, 659 * c, 680 * c, 880 * c, 880 * c, 830 * c},
                    new double[]{0.42, 0.9}, 5.5, 0.012, det == 0 ? 0.8 : 0.45, 1.0);
        }
        lowpass(b, 6000, 1, false);
        b = reverb(b, 0.25, 0.8, 0.45);
        normalizePeak(b, -6);
        fades(b, 0.004, 0.15);
        return b;
    }

    /** A short descending glassy blip. */
    static double[] wispHurt() {
        double[] b = buffer(0.45);
        double ph = 0, ph2 = 0;
        for (int j = 0; j < b.length; j++) {
            double t = j / (double) RATE, u = clamp(t / 0.32, 0, 1);
            double f = 1900 * Math.pow(650 / 1900.0, Math.pow(u, 0.75)) * (1 + 0.018 * Math.sin(TWO_PI * 24 * t));
            ph += TWO_PI * f / RATE;
            ph2 += TWO_PI * f * 2.405 / RATE;
            double env = riseCos(t / 0.003) * Math.exp(-t / 0.13) * riseCos((0.45 - t) / 0.1);
            b[j] = env * (Math.sin(ph) + 0.16 * Math.exp(-t / 0.05) * Math.sin(ph2));
        }
        strike(b, 0, 2100, 0.25, 0.05, CRYSTAL, 0.001, false);
        lowpass(b, 8500, 1, false);
        b = reverb(b, 0.12, 0.75, 0.4);
        normalizePeak(b, -4);
        fades(b, 0.001, 0.06);
        return b;
    }

    // ================================================================ music: "Fairy Garden" lullaby

    /** One eighth note at the base tempo (6/8, dotted quarter = 50). */
    static final double EIGHTH = 0.40;

    /** Music-box melody, one 6/8 bar per entry, "note:eighths" tokens, "r" for rests. */
    static final String[] TUNE = {
            // intro: harp alone
            "r:6", "r:6", "r:6", "r:6",
            // A
            "D5:2 G5:1 B5:2 A5:1", "A5:2 F#5:1 D5:3", "E5:2 G5:1 B5:2 A5:1", "G5:2 E5:1 G5:3",
            "B5:2 C6:1 D6:2 B5:1", "C6:2 B5:1 A5:2 E5:1", "F#5:2 G5:1 A5:2 B5:1", "A5:3 r:3",
            // A'
            "D5:2 G5:1 B5:2 A5:1", "A5:2 F#5:1 D5:3", "E5:2 G5:1 B5:2 E6:1", "E6:2 D6:1 C6:2 G5:1",
            "A5:2 C6:1 E6:2 C6:1", "D6:2 C6:1 A5:2 F#5:1", "G5:3 B5:2 D6:1", "G6:3 r:3",
            // B
            "G5:2 F#5:1 G5:2 B5:1", "F#5:2 E5:1 D5:3", "E5:2 F#5:1 G5:2 C6:1", "B5:2 A5:1 G5:3",
            "A5:2 G5:1 A5:2 C6:1", "B5:2 A5:1 G5:2 E5:1", "E5:2 G5:1 C6:2 E6:1", "D6:3 r:3",
            // A''
            "D5:2 G5:1 B5:2 A5:1", "A5:2 F#5:1 D5:3", "E5:2 G5:1 B5:2 E6:1", "G6:2 E6:1 C6:2 E6:1",
            "D6:2 B5:1 G5:2 B5:1", "C6:2 E6:1 A5:2 C6:1", "B5:2 A5:1 C6:2 F#5:1", "G5:3 r:3",
            // coda
            "G5:2 C6:1 E6:2 D6:1", "Eb6:3 D6:2 C6:1", "B5:2 A5:1 G5:3", "r:6",
    };

    static final String[] HARMONY = {
            "G", "Em", "C", "D",
            "G", "D", "Em", "C", "G", "Am", "D", "D",
            "G", "D", "Em", "C", "Am", "D7", "G", "G",
            "Em", "Bm", "C", "G", "Am", "Em", "C", "D",
            "G", "D", "Em", "C", "G", "Am", "D7", "G",
            "C", "Cm", "G", "G",
    };

    record Chord(int root, int third, boolean dom7) {
    }

    /** Parses "G", "Em", "D7"; the root lands between B2 and Bb3 for the harp. */
    static Chord chord(String name) {
        boolean dom7 = name.endsWith("7"), minor = name.endsWith("m");
        int pc = pitchClass(name.replace("7", "").replace("m", ""));
        return new Chord(47 + Math.floorMod(pc - 11, 12), minor ? 3 : 4, dom7);
    }

    static double sectionGain(int bar) {
        if (bar < 4) return 0.9;
        if (bar < 20) return 1.0;
        if (bar < 28) return 0.88;
        if (bar < 36) return 1.06;
        return new double[]{0.9, 0.85, 0.8, 0.8}[bar - 36];
    }

    /** Ritardando over the coda. */
    static double stretch(int bar) {
        return bar < 36 ? 1.0 : new double[]{1.03, 1.07, 1.13, 1.2}[bar - 36];
    }

    static int[] harpPattern(Chord c, int bar) {
        int t = c.third();
        if (c.dom7()) return new int[]{0, 7, 10, 12 + t, 12, 7};
        if (bar >= 28 && bar < 36) return new int[]{0, 7, 12, 12 + t, 19, 12 + t};
        return new int[]{0, 7, 12, 12 + t, 12, 7};
    }

    static double harpTau(double f) {
        return 1.6 * Math.pow(220 / f, 0.4);
    }

    /** Soft sustained chord tone: two slightly detuned sines with slow attack and release. */
    static void padTone(double[] b, double start, double dur, double f, double amp) {
        int s0 = samples(start), n = samples(dur + 1.2);
        for (int v = 0; v < 2; v++) {
            double w = TWO_PI * f * (v == 0 ? 1 : cents(6)) / RATE;
            for (int j = 0; j < n; j++) {
                double t = j / (double) RATE;
                double env = riseCos(t / 0.8) * (t > dur ? riseCos(1 - (t - dur) / 1.2) : 1);
                put(b, s0 + j, amp * 0.6 * env * Math.sin(w * j), false);
            }
        }
    }

    static double[] music() {
        Random r = rng("fairy_garden");
        int bars = TUNE.length;
        double[] start = new double[bars + 1];
        start[0] = 0.25;
        for (int i = 0; i < bars; i++) start[i + 1] = start[i] + 6 * EIGHTH * stretch(i);
        double[] mix = new double[samples(start[bars] + 7.0)];

        // warm pad, one tone per run of repeated chords
        for (int i = 0; i < bars; ) {
            int j = i;
            while (j + 1 < bars && HARMONY[j + 1].equals(HARMONY[i])) j++;
            double dur = start[j + 1] - start[i] + (j == bars - 1 ? 3.5 : 0);
            Chord c = chord(HARMONY[i]);
            for (int iv : new int[]{12, 12 + c.third(), 19}) padTone(mix, start[i], dur, midiHz(c.root() + iv), 0.028 * sectionGain(i));
            i = j + 1;
        }

        for (int bar = 0; bar < bars; bar++) {
            double t0 = start[bar], e = EIGHTH * stretch(bar), g = sectionGain(bar);
            Chord c = chord(HARMONY[bar]);
            if (bar == bars - 1) {
                // final rolled chord on the harp, two last celesta notes
                int[] roll = {-12, -5, 0, 4, 7, 12, 16, 19};
                for (int k = 0; k < roll.length; k++) {
                    double f = midiHz(c.root() + roll[k]);
                    strike(mix, t0 + 0.075 * k, f, (0.2 - 0.008 * k) * g, harpTau(f) * 1.3, HARP, 0.004, false);
                }
                strike(mix, t0, midiHz(c.root() - 12), 0.18 * g, 2.4, BASS, 0.02, false);
                strike(mix, t0 + 0.75, hz("B5"), 0.13, 1.2, CELESTA, 0.004, false);
                strike(mix, t0 + 1.25, hz("G6"), 0.11, 1.4, CELESTA, 0.004, false);
                continue;
            }
            // flowing harp arpeggio in eighths
            int[] pat = harpPattern(c, bar);
            for (int k = 0; k < 6; k++) {
                double f = midiHz(c.root() + pat[k]);
                double vel = (k == 0 ? 0.24 : k == 3 ? 0.2 : 0.155) * g * rand(r, 0.92, 1.05);
                strike(mix, t0 + k * e + rand(r, -0.006, 0.006), f, vel, harpTau(f), HARP, 0.004, false);
            }
            strike(mix, t0, midiHz(c.root() - 12), 0.2 * g, 1.8, BASS, 0.015, false);
            // music-box melody (doubled an octave up on celesta in the last verse)
            double beat = 0;
            for (String tok : TUNE[bar].split(" ")) {
                String[] p = tok.split(":");
                double d = Double.parseDouble(p[1]);
                if (!p[0].equals("r")) {
                    int m = midi(p[0]);
                    double f = midiHz(m), t = t0 + beat * e + rand(r, -0.005, 0.005);
                    double vel = (0.4 + (beat == 0 || beat == 3 ? 0.05 : 0) + rand(r, 0, 0.03)) * g;
                    strike(mix, t, f, vel, 1.4 * Math.pow(523.25 / f, 0.45), MUSIC_BOX, 0.002, false);
                    if (bar >= 28 && bar < 36 && m <= 86)
                        strike(mix, t + 0.004, midiHz(m + 12), 0.1 * g, 0.9, CELESTA, 0.004, false);
                }
                beat += d;
            }
            // celesta twinkles in the intro and the B section
            if (bar == 1 || bar == 3 || (bar >= 20 && bar <= 26 && bar % 2 == 0)) {
                int[] tones = {c.root() + 36, c.root() + 36 + c.third(), c.root() + 43};
                strike(mix, t0 + 4 * e, midiHz(tones[r.nextInt(3)]), 0.09, 1.0, CELESTA, 0.004, false);
            }
        }
        lowpass(mix, 9500, 1, false);
        highpass(mix, 35, false);
        mix = reverb(mix, 0.3, 0.85, 0.4);
        normalizePeak(mix, -1.5);
        fades(mix, 0.01, 4.5);
        return mix;
    }

    // ================================================================ output

    record Sound(String path, double[] data, boolean loop, int quality) {
    }

    static void writeWav(double[] s, File f) throws IOException {
        f.getParentFile().mkdirs();
        try (DataOutputStream o = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)))) {
            int dataLen = s.length * 2;
            o.writeBytes("RIFF");
            o.writeInt(Integer.reverseBytes(36 + dataLen));
            o.writeBytes("WAVEfmt ");
            o.writeInt(Integer.reverseBytes(16));
            o.writeShort(Short.reverseBytes((short) 1));
            o.writeShort(Short.reverseBytes((short) 1));
            o.writeInt(Integer.reverseBytes(RATE));
            o.writeInt(Integer.reverseBytes(RATE * 2));
            o.writeShort(Short.reverseBytes((short) 2));
            o.writeShort(Short.reverseBytes((short) 16));
            o.writeBytes("data");
            o.writeInt(Integer.reverseBytes(dataLen));
            for (double v : s) o.writeShort(Short.reverseBytes((short) Math.round(clamp(v, -1, 1) * 32767)));
        }
    }

    /** Peak, RMS and an RMS-frequency estimate (from the first difference) - a quick brightness sanity check. */
    static String stats(double[] b) {
        double d = 0;
        for (int i = 1; i < b.length; i++) d += (b[i] - b[i - 1]) * (b[i] - b[i - 1]);
        double ratio = Math.sqrt(d / (b.length - 1)) / rms(b);
        double hzEst = Math.asin(clamp(ratio / 2, 0, 1)) * RATE / Math.PI;
        return String.format("%6.2fs  peak %6.1f dBFS  rms %6.1f dBFS  ~%5.0f Hz",
                b.length / (double) RATE, gainToDb(peak(b)), gainToDb(rms(b)), hzEst);
    }

    /** For a loop, the step from the last sample back to the first must look like any other step. */
    static String wrapCheck(double[] b) {
        int n = b.length, w = samples(0.25);
        double max = 0, sq = 0;
        for (int i = 1; i < n; i++) {
            double s = Math.abs(b[i] - b[i - 1]);
            max = Math.max(max, s);
            sq += s * s;
        }
        double head = rms(Arrays.copyOfRange(b, 0, w)), tail = rms(Arrays.copyOfRange(b, n - w, n));
        return String.format("wrap step %.5f (typical %.5f, max %.5f); level last/first 250 ms %.1f / %.1f dBFS",
                Math.abs(b[0] - b[n - 1]), Math.sqrt(sq / (n - 1)), max, gainToDb(tail), gainToDb(head));
    }

    static void encode(File wavRoot, File oggRoot, List<Sound> sounds) throws IOException, InterruptedException {
        String ffmpeg = System.getenv().getOrDefault("FFMPEG", "ffmpeg");
        for (Sound s : sounds) {
            File wav = new File(wavRoot, s.path() + ".wav"), ogg = new File(oggRoot, s.path() + ".ogg");
            ogg.getParentFile().mkdirs();
            Process p = new ProcessBuilder(ffmpeg, "-y", "-loglevel", "error", "-i", wav.getPath(),
                    "-ac", "1", "-c:a", "libvorbis", "-q:a", String.valueOf(s.quality()),
                    "-map_metadata", "-1", "-fflags", "+bitexact", "-flags:a", "+bitexact", ogg.getPath())
                    .inheritIO().start();
            if (p.waitFor() != 0) throw new IOException("ffmpeg failed for " + wav);
            if (!wav.delete()) throw new IOException("could not delete " + wav);
            System.out.println("  encoded " + s.path() + ".ogg");
        }
        for (Sound s : sounds) { // tidy the now-empty temporary folders
            File d = new File(wavRoot, s.path()).getParentFile();
            while (d != null && !d.equals(wavRoot.getParentFile()) && d.isDirectory() && d.list().length == 0) {
                d.delete();
                d = d.getParentFile();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: java tools/audiogen/AudioGenerator.java <outdir> [--ogg <soundsDir>]");
            System.exit(2);
        }
        File out = new File(args[0]).getAbsoluteFile();
        File ogg = null;
        for (int i = 1; i < args.length; i++) if (args[i].equals("--ogg") && i + 1 < args.length) ogg = new File(args[++i]).getAbsoluteFile();
        System.out.println("Fairy Garden audio generator -> " + out);

        List<Sound> sounds = new ArrayList<>();
        sounds.add(new Sound("ambient/birds1", birds1(), false, 4));
        sounds.add(new Sound("ambient/birds2", birds2(), false, 4));
        sounds.add(new Sound("ambient/birds3", birds3(), false, 4));
        sounds.add(new Sound("ambient/birds4", birds4(), false, 4));
        sounds.add(new Sound("ambient/lavender_loop", lavenderLoop(), true, 4));
        sounds.add(new Sound("ambient/bees1", bees("bees1", 3.2, 222, 0.5, 0.5, 0.04, 0), false, 4));
        sounds.add(new Sound("ambient/bees2", bees("bees2", 3.6, 196, 0.42, 0.75, 0.03, 0.018), false, 4));
        sounds.add(new Sound("ambient/moonlight_loop", moonlightLoop(), true, 4));
        sounds.add(new Sound("ambient/night1", owl(), false, 4));
        sounds.add(new Sound("ambient/night2", windChimes(), false, 4));
        sounds.add(new Sound("ambient/night3", sparkleSweep(), false, 4));
        sounds.add(new Sound("ambient/glade_loop", gladeLoop(), true, 4));
        sounds.add(new Sound("ambient/chimes1", chimes("chimes1", new String[]{"C6", "E6", "A6"},
                new double[]{0.01, 0.08, 0.15}, new double[]{0.9, 0.8, 0.85}, 0.7, 2.0), false, 4));
        sounds.add(new Sound("ambient/chimes2", chimes("chimes2", new String[]{"A6", "G6", "E6", "D6"},
                new double[]{0.01, 0.07, 0.13, 0.19}, new double[]{0.9, 0.75, 0.7, 0.8}, 0.75, 2.2), false, 4));
        sounds.add(new Sound("ambient/chimes3", chimes("chimes3", new String[]{"D6", "G6", "A6", "D7"},
                new double[]{0.01, 0.035, 0.06, 0.085}, new double[]{0.7, 0.6, 0.6, 0.55}, 0.85, 2.4), false, 4));
        sounds.add(new Sound("ambient/chimes4", chimes("chimes4", new String[]{"E6", "A6", "C7", "G6", "D7"},
                new double[]{0.01, 0.1, 0.16, 0.29, 0.38}, new double[]{0.8, 0.6, 0.55, 0.65, 0.5}, 0.65, 2.6), false, 4));
        sounds.add(new Sound("music/fairy_garden", music(), false, 5));
        sounds.add(new Sound("block/crystal_chime1", crystalChime("crystal_chime1", "D6"), false, 4));
        sounds.add(new Sound("block/crystal_chime2", crystalChime("crystal_chime2", "F#6"), false, 4));
        sounds.add(new Sound("block/crystal_chime3", crystalChime("crystal_chime3", "A6"), false, 4));
        sounds.add(new Sound("block/crystal_break1", crystalBreak("crystal_break1", 0.75, 34), false, 4));
        sounds.add(new Sound("block/crystal_break2", crystalBreak("crystal_break2", 0.9, 40), false, 4));
        sounds.add(new Sound("block/crystal_step1", crystalStep("crystal_step1", 0.22, 2900), false, 4));
        sounds.add(new Sound("block/crystal_step2", crystalStep("crystal_step2", 0.2, 3300), false, 4));
        sounds.add(new Sound("block/moonflower_bloom", moonflowerBloom(), false, 4));
        sounds.add(new Sound("item/fairy_dust", fairyDust(), false, 4));
        sounds.add(new Sound("entity/wisp_ambient1", wisp1(), false, 4));
        sounds.add(new Sound("entity/wisp_ambient2", wisp2(), false, 4));
        sounds.add(new Sound("entity/wisp_hurt", wispHurt(), false, 4));

        for (int i = 0; i < sounds.size(); i++) { // one-shots get 8 ms of silence up front, so codec noise
            Sound s = sounds.get(i);              // around a sharp attack can never land on the first sample
            if (!s.loop()) {
                double[] padded = new double[s.data().length + samples(0.008)];
                System.arraycopy(s.data(), 0, padded, samples(0.008), s.data().length);
                sounds.set(i, new Sound(s.path(), padded, false, s.quality()));
            }
        }
        for (Sound s : sounds) {
            writeWav(s.data(), new File(out, s.path() + ".wav"));
            System.out.printf("  %-28s %s%n", s.path(), stats(s.data()));
            if (s.loop()) System.out.printf("  %-28s %s%n", "", wrapCheck(s.data()));
        }
        if (ogg != null) encode(out, ogg, sounds);
    }
}
