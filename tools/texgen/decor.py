"""Giant flower parts, crystals, lanterns and the lavender bale."""
import math

from .core import Img, Rng, hash01, mix, ramp, value_noise, lighten, darken, with_alpha
from . import palette as P


def giant_stem(top):
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            if top:
                d = math.hypot(x - 7.5, y - 7.5)
                t = 0.55 + (0.12 if int(d) % 3 == 0 else 0) - (0.2 if x in (0, 15) or y in (0, 15) else 0)
            else:
                t = 0.5 + (value_noise(x, y * 0.3, 2, "stem", wrap=8) - 0.5) * 0.3
                if x % 4 == 1:
                    t += 0.15
                if x % 4 == 3:
                    t -= 0.12
            img.set(x, y, ramp(P.GLADE_STEM, t))
    return img


def giant_petals(seed, pal):
    """Velvety petal surface with soft veins; tiles seamlessly."""
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            n = value_noise(x, y, 5, seed, wrap=3)
            vein = abs(math.sin((x + y * 0.35) / 16 * math.tau * 2))
            t = 0.55 + (n - 0.5) * 0.3 - (0.12 if vein < 0.12 else 0) + (0.06 if hash01(seed, x, y) < 0.08 else 0)
            img.set(x, y, ramp(pal, t))
    return img


def pistil():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            n = value_noise(x, y, 3, "pistil", wrap=5)
            img.set(x, y, ramp(P.BUTTER, 0.35 + n * 0.4))
    for y in range(0, 16, 3):
        for x in range((y // 3) % 3, 16, 3):
            img.set(x, y, P.BUTTER[4])
            img.set(x + 1, y, P.CREAM[4])
    return img


def crystal_block():
    """Faceted pastel crystal: Voronoi facets, each a soft gradient, with bright edges."""
    img = Img(16, 16)
    rng = Rng("crystal")
    sites = [(rng.uniform(0, 16), rng.uniform(0, 16), rng.pick([P.PINK, P.LAVENDER, P.BLUE, P.MAGENTA])) for _ in range(7)]
    for y in range(16):
        for x in range(16):
            best = None
            second = 99
            for sx, sy, pal in sites:
                for ox in (-16, 0, 16):
                    for oy in (-16, 0, 16):
                        d = math.hypot(x + 0.5 - sx - ox, y + 0.5 - sy - oy)
                        if best is None or d < best[0]:
                            if best is not None:
                                second = best[0]
                            best = (d, pal, sx + ox, sy + oy)
                        elif d < second:
                            second = d
            d, pal, sx, sy = best
            edge = second - d < 0.9
            t = 0.7 - (x - sx + y - sy) * 0.04
            img.set(x, y, pal[4] if edge else ramp(pal, max(0.45, min(0.95, t))))
    return img


def crystal_cluster():
    img = Img(16, 16)
    points = [(8, 1, 0, P.LAVENDER), (5, 5, -1, P.PINK), (11, 4, 1, P.BLUE), (3, 9, -1, P.MAGENTA), (13, 9, 1, P.PINK)]
    for x, top, lean, pal in points:
        for y in range(top, 16):
            k = y - top
            w = min(k // 2, 1)
            cx = x + round(lean * k * 0.25)
            for dx in range(-w, w + 1):
                img.set(cx + dx, y, pal[4] if dx < 0 else (pal[3] if dx == 0 else pal[2]))
        img.set(x, top, "#FFFFFF")
    return img


def lantern(metal, glow, frames=3):
    """Lantern atlas in vanilla's layout (body 0-5 x 2-8, plates 0-5 x 9-14, cap 1-4 x 0-1, handle 11-13), animated."""
    img = Img(16, 16 * frames)
    for f in range(frames):
        oy = f * 16
        flick = [0.0, 0.25, 0.1][f % 3]
        # cap sides
        img.rect(1, oy + 0, 4, oy + 1, metal[1])
        img.rect(1, oy + 0, 4, oy + 0, metal[2])
        # body sides
        for y in range(2, 9):
            for x in range(0, 6):
                border = x in (0, 5) or y in (2, 8)
                if border:
                    img.set(x, oy + y, metal[2] if x == 0 or y == 2 else metal[1])
                else:
                    t = 0.35 + flick + (1 - abs(y - 5) / 3.0) * 0.35
                    img.set(x, oy + y, ramp(glow, min(1.0, t)))
        img.set(2, oy + 4, "#FFFFFF")
        img.set(3, oy + 6 - (f % 2), "#FFFFFF")
        # top/bottom plates (and cap top inside)
        for y in range(9, 15):
            for x in range(0, 6):
                edge = x in (0, 5) or y in (9, 14)
                img.set(x, oy + y, metal[1] if edge else metal[2])
        img.rect(2, oy + 11, 3, oy + 12, metal[3])
        # handle / chain
        for y in range(1, 12):
            for x in range(11, 14):
                if (y in (1, 2) or 6 <= y <= 11) and (x != 12 or y % 2 == 0):
                    img.set(x, oy + y, metal[3] if x == 11 else metal[2])
    return img


def lantern_item(metal, glow):
    img = Img(16, 16)
    img.rect(6, 1, 9, 2, metal[2])
    img.set(7, 0, metal[3])
    img.set(8, 0, metal[3])
    img.rect(5, 3, 10, 3, metal[1])
    for y in range(4, 12):
        for x in range(5, 11):
            border = x in (5, 10)
            img.set(x, y, metal[2] if border else ramp(glow, 0.4 + (1 - abs(y - 7.5) / 4) * 0.5))
    img.rect(5, 12, 10, 13, metal[1])
    img.set(7, 6, "#FFFFFF")
    img.set(8, 9, "#FFFFFF")
    return img


def lavender_bale(top):
    img = Img(16, 16)
    if top:
        for y in range(16):
            for x in range(16):
                img.set(x, y, ramp(P.SAGE, 0.3 + hash01("bt", x, y) * 0.4))
        for y in range(0, 16, 2):
            for x in range((y // 2) % 2, 16, 2):
                img.set(x, y, P.LAVENDER[2] if hash01("bp", x, y) < 0.6 else P.SAGE[4])
        for i in range(16):
            for j in (0, 15):
                img.set(i, j, P.CREAM[2])
                img.set(j, i, P.CREAM[2])
    else:
        for y in range(16):
            for x in range(16):
                if y < 6:
                    t = 0.35 + hash01("bud", x, y) * 0.5
                    img.set(x, y, ramp(P.LAVENDER, t) if (x + y) % 3 else P.SAGE[2])
                else:
                    img.set(x, y, ramp(P.SAGE, 0.35 + (0.2 if x % 3 == 0 else 0) + hash01("st", x, y) * 0.15))
        img.rect(0, 9, 15, 10, P.PINK[2])
        img.rect(0, 9, 15, 9, P.PINK[3])
    return img
