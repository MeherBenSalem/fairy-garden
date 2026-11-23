"""Core helpers for the Fairy Garden texture generator: PNG I/O, colours, deterministic noise and drawing."""
import colorsys
import math
import struct
import zlib
from pathlib import Path

MODID = "fairygarden"
ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / "src" / "main" / "resources"
TEX = RES / "assets" / MODID / "textures"


# --------------------------------------------------------------------------- colours

def C(value, alpha=255):
    """'#RRGGBB', (r,g,b) or (r,g,b,a) -> RGBA tuple."""
    if isinstance(value, str):
        v = value.lstrip("#")
        return (int(v[0:2], 16), int(v[2:4], 16), int(v[4:6], 16), alpha)
    if len(value) == 3:
        return (value[0], value[1], value[2], alpha)
    return tuple(value)


def clamp8(v):
    return max(0, min(255, int(round(v))))


def mix(a, b, t):
    a, b = C(a), C(b)
    return tuple(clamp8(a[i] + (b[i] - a[i]) * t) for i in range(4))


def lighten(c, t):
    c = C(c)
    return mix(c, (255, 255, 255, c[3]), t)


def darken(c, t):
    c = C(c)
    return mix(c, (0, 0, 0, c[3]), t)


def with_alpha(c, a):
    c = C(c)
    return (c[0], c[1], c[2], clamp8(a))


def hue_shift(c, dh, ds=0.0, dv=0.0):
    c = C(c)
    h, s, v = colorsys.rgb_to_hsv(c[0] / 255, c[1] / 255, c[2] / 255)
    r, g, b = colorsys.hsv_to_rgb((h + dh) % 1.0, max(0, min(1, s + ds)), max(0, min(1, v + dv)))
    return (clamp8(r * 255), clamp8(g * 255), clamp8(b * 255), c[3])


def ramp(colors, t):
    """Sample a list of colours as an even gradient, t in [0,1]."""
    t = max(0.0, min(1.0, t))
    pos = t * (len(colors) - 1)
    i = min(int(pos), len(colors) - 2)
    return mix(colors[i], colors[i + 1], pos - i)


# --------------------------------------------------------------------------- noise

def hash01(*args):
    """Deterministic pseudo-random float in [0,1) from integer/str args."""
    h = 2166136261
    for a in args:
        if isinstance(a, str):
            for ch in a.encode():
                h = ((h ^ ch) * 16777619) & 0xFFFFFFFF
        else:
            a = int(a) & 0xFFFFFFFF
            for shift in (0, 8, 16, 24):
                h = ((h ^ ((a >> shift) & 0xFF)) * 16777619) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 1274126177) & 0xFFFFFFFF
    h ^= h >> 16
    return h / 4294967296.0


class Rng:
    """Tiny deterministic RNG (xorshift) seeded from a string."""

    def __init__(self, seed):
        self.state = int(hash01("seed", seed) * 4294967295) | 1

    def next(self):
        x = self.state
        x ^= (x << 13) & 0xFFFFFFFF
        x ^= x >> 17
        x ^= (x << 5) & 0xFFFFFFFF
        self.state = x & 0xFFFFFFFF
        return self.state / 4294967296.0

    def range(self, a, b):
        return a + int(self.next() * (b - a + 1))

    def chance(self, p):
        return self.next() < p

    def pick(self, items):
        return items[int(self.next() * len(items)) % len(items)]

    def uniform(self, a, b):
        return a + self.next() * (b - a)


def value_noise(x, y, scale, seed, wrap=None):
    """Smooth 2D value noise in [0,1]; optionally tileable with period `wrap` (in noise cells)."""
    fx, fy = x / scale, y / scale
    x0, y0 = math.floor(fx), math.floor(fy)
    tx, ty = fx - x0, fy - y0
    tx = tx * tx * (3 - 2 * tx)
    ty = ty * ty * (3 - 2 * ty)

    def corner(ix, iy):
        if wrap:
            ix %= wrap
            iy %= wrap
        return hash01(seed, ix, iy)

    a = corner(x0, y0)
    b = corner(x0 + 1, y0)
    c = corner(x0, y0 + 1)
    d = corner(x0 + 1, y0 + 1)
    return (a + (b - a) * tx) + ((c + (d - c) * tx) - (a + (b - a) * tx)) * ty


# --------------------------------------------------------------------------- images

class Img:
    def __init__(self, w, h, fill=(0, 0, 0, 0)):
        self.w, self.h = w, h
        self.px = [C(fill)] * (w * h)

    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.px[y * self.w + x]
        return (0, 0, 0, 0)

    def set(self, x, y, c):
        x, y = int(x), int(y)
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[y * self.w + x] = C(c)

    def blend(self, x, y, c):
        """Alpha-composite c over the existing pixel."""
        x, y = int(x), int(y)
        if not (0 <= x < self.w and 0 <= y < self.h):
            return
        c = C(c)
        if c[3] >= 255:
            self.px[y * self.w + x] = c
            return
        if c[3] <= 0:
            return
        d = self.px[y * self.w + x]
        a = c[3] / 255
        out_a = a + d[3] / 255 * (1 - a)
        if out_a <= 0:
            return
        rgb = [(c[i] * a + d[i] * (d[3] / 255) * (1 - a)) / out_a for i in range(3)]
        self.px[y * self.w + x] = (clamp8(rgb[0]), clamp8(rgb[1]), clamp8(rgb[2]), clamp8(out_a * 255))

    def rect(self, x0, y0, x1, y1, c):
        """Inclusive rectangle fill."""
        for y in range(int(y0), int(y1) + 1):
            for x in range(int(x0), int(x1) + 1):
                self.set(x, y, c)

    def fill(self, fn):
        """fn(x, y) -> colour or None."""
        for y in range(self.h):
            for x in range(self.w):
                c = fn(x, y)
                if c is not None:
                    self.set(x, y, c)

    def line(self, x0, y0, x1, y1, c):
        x0, y0, x1, y1 = int(round(x0)), int(round(y0)), int(round(x1)), int(round(y1))
        dx, dy = abs(x1 - x0), -abs(y1 - y0)
        sx = 1 if x0 < x1 else -1
        sy = 1 if y0 < y1 else -1
        err = dx + dy
        while True:
            self.set(x0, y0, c)
            if x0 == x1 and y0 == y1:
                break
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                x0 += sx
            if e2 <= dx:
                err += dx
                y0 += sy

    def disc(self, cx, cy, r, c, blend=False):
        for y in range(int(cy - r - 1), int(cy + r + 2)):
            for x in range(int(cx - r - 1), int(cx + r + 2)):
                if (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2 <= r * r:
                    (self.blend if blend else self.set)(x, y, c)

    def paste(self, other, ox, oy):
        for y in range(other.h):
            for x in range(other.w):
                c = other.get(x, y)
                if c[3] > 0:
                    self.blend(ox + x, oy + y, c)

    def copy(self):
        out = Img(self.w, self.h)
        out.px = list(self.px)
        return out

    def flip_x(self):
        out = Img(self.w, self.h)
        for y in range(self.h):
            for x in range(self.w):
                out.set(self.w - 1 - x, y, self.get(x, y))
        return out

    def outline(self, c, only_empty=True):
        """Draws a 1px outline around opaque pixels."""
        src = self.copy()
        for y in range(self.h):
            for x in range(self.w):
                if src.get(x, y)[3] > 0:
                    continue
                if any(src.get(x + dx, y + dy)[3] > 0 for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                    self.set(x, y, c)

    def save(self, path):
        path = Path(path)
        path.parent.mkdir(parents=True, exist_ok=True)
        raw = bytearray()
        for y in range(self.h):
            raw.append(0)
            for x in range(self.w):
                raw.extend(self.px[y * self.w + x])

        def chunk(tag, data):
            body = tag + data
            return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

        png = b"\x89PNG\r\n\x1a\n"
        png += chunk(b"IHDR", struct.pack(">IIBBBBB", self.w, self.h, 8, 6, 0, 0, 0))
        png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
        png += chunk(b"IEND", b"")
        path.write_bytes(png)


def load_png(path):
    """Minimal PNG reader (8-bit RGBA/RGB/palette/grey, non-interlaced) -> Img. Accepts a path or raw bytes."""
    data = bytes(path) if isinstance(path, (bytes, bytearray)) else Path(path).read_bytes()
    pos = 8
    width = height = 0
    color_type = bit_depth = 0
    palette = []
    trns = b""
    idat = b""
    while pos < len(data):
        length = struct.unpack(">I", data[pos:pos + 4])[0]
        tag = data[pos + 4:pos + 8]
        body = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if tag == b"IHDR":
            width, height, bit_depth, color_type = struct.unpack(">IIBB", body[:10])
        elif tag == b"PLTE":
            palette = [tuple(body[i:i + 3]) for i in range(0, len(body), 3)]
        elif tag == b"tRNS":
            trns = body
        elif tag == b"IDAT":
            idat += body
    raw = zlib.decompress(idat)
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[color_type]
    if bit_depth != 8:
        if color_type == 3 and bit_depth in (1, 2, 4):
            pass
        else:
            raise ValueError("unsupported bit depth %d in %s" % (bit_depth, path))
    bpp = max(1, channels * bit_depth // 8)
    stride = (width * channels * bit_depth + 7) // 8
    img = Img(width, height)
    prev = bytearray(stride)
    i = 0
    for y in range(height):
        f = raw[i]
        line = bytearray(raw[i + 1:i + 1 + stride])
        i += 1 + stride
        for x in range(stride):
            a = line[x - bpp] if x >= bpp else 0
            b = prev[x]
            c = prev[x - bpp] if x >= bpp else 0
            if f == 1:
                line[x] = (line[x] + a) & 0xFF
            elif f == 2:
                line[x] = (line[x] + b) & 0xFF
            elif f == 3:
                line[x] = (line[x] + (a + b) // 2) & 0xFF
            elif f == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pr = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                line[x] = (line[x] + pr) & 0xFF
        prev = line
        for x in range(width):
            if color_type == 3:
                if bit_depth == 8:
                    idx = line[x]
                else:
                    per = 8 // bit_depth
                    idx = (line[x // per] >> (8 - bit_depth * (x % per + 1))) & ((1 << bit_depth) - 1)
                r, g, b = palette[idx]
                a = trns[idx] if idx < len(trns) else 255
                img.set(x, y, (r, g, b, a))
            elif color_type == 6:
                img.set(x, y, tuple(line[x * 4:x * 4 + 4]))
            elif color_type == 2:
                img.set(x, y, tuple(line[x * 3:x * 3 + 3]) + (255,))
            elif color_type == 4:
                g, a = line[x * 2:x * 2 + 2]
                img.set(x, y, (g, g, g, a))
            else:
                g = line[x]
                img.set(x, y, (g, g, g, 255))
    return img


def save(img, *parts):
    img.save(TEX.joinpath(*parts))


def write_text(rel, text):
    path = TEX / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")
