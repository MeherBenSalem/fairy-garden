"""Leaves, saplings, flowers, grasses, bushes, crops, mushrooms and other plant textures (16x16 sprites)."""
import math

from .core import Img, Rng, hash01, mix, ramp, value_noise, darken, lighten, with_alpha, C
from . import palette as P


# --------------------------------------------------------------------------- primitives

def stem_line(img, points, pal, width=1):
    """Polyline stem with a darker left edge for roundness."""
    for (x0, y0), (x1, y1) in zip(points, points[1:]):
        steps = max(abs(x1 - x0), abs(y1 - y0), 1) * 2
        for i in range(steps + 1):
            t = i / steps
            x = x0 + (x1 - x0) * t
            y = y0 + (y1 - y0) * t
            img.set(round(x), round(y), pal[1])
            if width > 1:
                img.set(round(x) + 1, round(y), pal[2])


def leaf(img, x, y, dx, dy, length, pal, width=1.4):
    """A pointed leaf blade growing from (x,y) in direction (dx,dy)."""
    norm = math.hypot(dx, dy) or 1
    dx, dy = dx / norm, dy / norm
    for i in range(int(length * 3)):
        t = i / (length * 3)
        cx = x + dx * length * t
        cy = y + dy * length * t
        w = width * math.sin(math.pi * min(1, t * 1.15)) + 0.2
        for k in range(-2, 3):
            px = cx - dy * k * 0.5
            py = cy + dx * k * 0.5
            if abs(k * 0.5) <= w:
                shade = 0.65 if k < 0 else 0.4
                if abs(k * 0.5) < 0.3:
                    shade = 0.8
                img.set(round(px), round(py), ramp(pal, shade))


def shaded_disc(img, cx, cy, r, pal, seed="", spots=None):
    for y in range(int(cy - r - 1), int(cy + r + 2)):
        for x in range(int(cx - r - 1), int(cx + r + 2)):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            d = math.hypot(dx, dy)
            if d <= r:
                light = 0.62 - (dx + dy) / (r * 4) - (d / r) * 0.18 + (hash01(seed, x, y) - 0.5) * 0.1
                img.set(x, y, ramp(pal, light))


def floret(img, cx, cy, pal, center, big=False):
    """A tiny 4/5-petal flower: plus shape with a coloured heart."""
    for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
        img.set(cx + dx, cy + dy, pal[3] if dy <= 0 else pal[2])
    if big:
        for dx, dy in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
            img.set(cx + dx, cy + dy, pal[2])
        for dx, dy in ((0, -2), (-2, 0), (2, 0), (0, 2)):
            img.set(cx + dx, cy + dy, pal[1])
    img.set(cx, cy, center)


# --------------------------------------------------------------------------- leaves & saplings

def leaves(seed, leaf_pal, flower_pal=None, flower_count=0, center=None, glow=None, accents=None):
    """Cutout leaves: leafy clumps with see-through gaps, optionally covered in blossoms.
    Returns (leaves, glow_overlay_or_None). Wraps around edges so it tiles."""
    rng = Rng(seed)
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            n = value_noise(x, y, 3, seed, wrap=5)
            if n > 0.32 or hash01(seed, "fill", x, y) < 0.2:
                t = 0.3 + n * 0.5 + (hash01(seed, "l", x, y) - 0.5) * 0.25
                img.set(x, y, ramp(leaf_pal, t))
    # leaf highlights / veins
    for i in range(18):
        x, y = rng.range(0, 15), rng.range(0, 15)
        if img.get(x, y)[3]:
            img.set(x, y, leaf_pal[4] if rng.chance(0.5) else leaf_pal[3])
    # a few green leaves peeking through a canopy of blossoms
    if accents:
        for i in range(14):
            x, y = rng.range(0, 15), rng.range(0, 15)
            if img.get(x, y)[3]:
                img.set(x, y, accents[2] if rng.chance(0.6) else accents[3])
    if flower_pal:
        for i in range(flower_count):
            cx, cy = rng.range(0, 15), rng.range(0, 15)
            big = rng.chance(0.5)
            pts = [(0, 0), (0, -1), (-1, 0), (1, 0), (0, 1)]
            if big:
                pts += [(-1, -1), (1, 1), (1, -1), (-1, 1)]
            for dx, dy in pts:
                x, y = (cx + dx) % 16, (cy + dy) % 16
                shade = 3 if dy < 0 or dx < 0 else 2
                if (dx, dy) == (0, 0):
                    img.set(x, y, center or flower_pal[1])
                else:
                    img.set(x, y, flower_pal[shade if not big or abs(dx) + abs(dy) < 2 else shade - 1])
    overlay = None
    if glow:
        overlay = Img(16, 16)
        for i in range(9):
            x, y = rng.range(0, 15), rng.range(0, 15)
            if img.get(x, y)[3]:
                overlay.set(x, y, glow[2] if rng.chance(0.4) else glow[1])
                if rng.chance(0.3):
                    overlay.set((x + 1) % 16, y, with_alpha(glow[0], 200))
    return img, overlay


def sapling(seed, stem_pal, crown_pal, blossom_pal=None):
    img = Img(16, 16)
    stem_line(img, [(8, 15), (8, 11), (7, 8)], stem_pal)
    leaf(img, 8, 13, -1, -0.6, 3, P.LEAF)
    shaded_disc(img, 7.5, 6.0, 3.6, crown_pal, seed)
    shaded_disc(img, 4.8, 8.0, 2.0, crown_pal, seed + "b")
    shaded_disc(img, 10.6, 7.5, 2.2, crown_pal, seed + "c")
    if blossom_pal:
        rng = Rng(seed)
        for i in range(6):
            x, y = rng.range(4, 11), rng.range(3, 9)
            if img.get(x, y)[3]:
                img.set(x, y, blossom_pal[min(len(blossom_pal) - 1, rng.range(3, 4))])
    return img


# --------------------------------------------------------------------------- small flowers

def rosy_daisy():
    img = Img(16, 16)
    stem_line(img, [(8, 15), (8, 10), (9, 6)], P.STEM)
    stem_line(img, [(8, 13), (5, 10)], P.STEM)
    leaf(img, 8, 12, 1, -0.5, 4, P.LEAF)
    leaf(img, 8, 14, -1, -0.4, 3.5, P.LEAF)

    def daisy(cx, cy, r):
        for y in range(int(cy - r - 1), int(cy + r + 2)):
            for x in range(int(cx - r - 1), int(cx + r + 2)):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                d = math.hypot(dx, dy)
                if d <= r:
                    ang = math.atan2(dy, dx)
                    gap = abs(math.sin(ang * 4)) < 0.18 and d > r * 0.55
                    if not gap:
                        img.set(x, y, P.PINK[3] if dy < 0 else P.PINK[2])
                    if d > r * 0.8:
                        img.set(x, y, P.PINK[4] if dy < 0 else P.PINK[2])
        img.disc(cx, cy, r * 0.42, P.BUTTER[2])
        img.set(int(cx), int(cy), P.BUTTER[1])

    daisy(9.0, 5.0, 3.8)
    daisy(5.0, 9.0, 2.4)
    return img


def lavender_plant():
    img = Img(16, 16)
    for sx, top, bend in ((5, 4, -1), (8, 2, 0), (11, 5, 1)):
        stem_line(img, [(sx, 15), (sx + bend * 0.5, 10), (sx + bend, top + 4)], P.SAGE)
        for i in range(6):
            y = top + i
            x = sx + bend + (1 if i % 2 else -1) * (1 if i < 5 else 0)
            img.set(sx + bend, y, P.LAVENDER[2 if i % 2 else 1])
            img.set(x, y, P.LAVENDER[3 if i % 2 else 2])
        img.set(sx + bend, top - 1, P.LAVENDER[3])
    leaf(img, 7, 15, -1, -1.4, 3, P.SAGE, 1)
    leaf(img, 9, 15, 1, -1.5, 3, P.SAGE, 1)
    return img


def cream_buttercup():
    img = Img(16, 16)
    stem_line(img, [(7, 15), (7, 10), (6, 7)], P.STEM)
    stem_line(img, [(8, 15), (10, 11), (11, 9)], P.STEM)
    leaf(img, 7, 13, -1, -0.3, 3.5, P.LEAF)
    leaf(img, 9, 14, 1, -0.6, 3, P.LEAF)

    def cup(cx, cy, s):
        rows = [(-1, 1), (-2, 2), (-2, 2), (-1, 1)] if s else [(-1, 1), (-1, 1), (0, 0)]
        for i, (a, b) in enumerate(rows):
            for dx in range(a, b + 1):
                shade = 4 if i == 0 else (3 if dx < 0 else 2)
                img.set(cx + dx, cy + i, P.CREAM[shade])
        img.set(cx, cy, P.BUTTER[2])
        img.set(cx - 1 if s else cx, cy + 1, P.BUTTER[3])

    cup(6, 3, True)
    cup(11, 6, False)
    return img


def mint_posy():
    img = Img(16, 16)
    for sx, ex, ey in ((8, 5, 7), (8, 8, 4), (8, 11, 6), (8, 7, 9), (8, 10, 9)):
        stem_line(img, [(sx, 15), (ex, ey + 1)], P.STEM)
    leaf(img, 8, 14, -1, -0.5, 3.5, P.LEAF)
    leaf(img, 8, 13, 1, -0.6, 3.5, P.LEAF)
    for cx, cy in ((5, 6), (8, 3), (11, 5), (7, 8), (10, 8)):
        floret(img, cx, cy, P.MINT, P.WHITE[4])
    return img


def starflower():
    img = Img(16, 16)
    stem_line(img, [(8, 15), (8, 9), (9, 6)], P.STEM)
    stem_line(img, [(8, 12), (5, 10)], P.STEM)
    leaf(img, 8, 13, 1, -0.3, 3.5, P.LEAF)

    def star(cx, cy, r):
        for y in range(int(cy - r - 1), int(cy + r + 2)):
            for x in range(int(cx - r - 1), int(cx + r + 2)):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                ang = math.atan2(dy, dx) + math.pi / 2
                lim = r * (0.45 + 0.55 * abs(math.cos(ang * 2.5)))
                if math.hypot(dx, dy) <= lim:
                    img.set(x, y, P.WHITE[4] if dy < 0.5 else P.WHITE[3])
        img.set(int(cx), int(cy), P.BUTTER[2])

    star(9.0, 5.0, 4.2)
    star(5.0, 9.0, 2.6)
    return img


def bluebell():
    img = Img(16, 16)
    stem_line(img, [(6, 15), (6, 9), (7, 5), (9, 3), (11, 4)], P.STEM)
    leaf(img, 6, 15, -1, -1.2, 5, P.LEAF, 1.2)
    leaf(img, 7, 15, 1, -1.6, 4, P.LEAF, 1.0)

    def bell(x, y):
        img.set(x, y, P.STEM[1])
        for dx in (-1, 0, 1):
            img.set(x + dx, y + 1, P.BLUE[3] if dx < 0 else P.BLUE[2])
            img.set(x + dx, y + 2, P.BLUE[2] if dx < 0 else P.BLUE[1])
        img.set(x - 2, y + 3, P.BLUE[3])
        img.set(x + 2, y + 3, P.BLUE[2])
        img.set(x, y + 3, P.BLUE[1])

    bell(11, 4)
    bell(8, 4)
    bell(6, 7)
    return img


def moonflower(open_):
    img = Img(16, 16)
    stem_line(img, [(8, 15), (8, 10), (8, 8)], P.MOON_STEM)
    leaf(img, 8, 13, -1, -0.4, 4, P.MOON_LEAF)
    leaf(img, 8, 11, 1, -0.5, 3.5, P.MOON_LEAF)
    if not open_:
        for i, y in enumerate(range(2, 9)):
            w = [0, 1, 1, 1, 1, 0, 0][i]
            for dx in range(-w, w + 1):
                shade = P.BLUE[4] if (dx + i) % 3 == 0 else P.BLUE[3]
                img.set(8 + dx, y, shade)
        img.set(8, 1, P.WHITE[4])
        img.set(7, 9, P.MOON_STEM[2])
        img.set(9, 9, P.MOON_STEM[2])
    else:
        cx, cy = 8.0, 5.0
        for y in range(0, 11):
            for x in range(1, 16):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                d = math.hypot(dx, dy)
                ang = math.atan2(dy, dx)
                lim = 4.6 + 0.9 * math.cos(ang * 5)
                if d <= lim:
                    t = d / lim
                    c = mix(P.GLOW_BLUE[2], P.BLUE[3], t * 0.8)
                    if abs(math.sin(ang * 5)) < 0.12 and d > 1.5:
                        c = P.BLUE[2]
                    img.set(x, y, c)
        img.disc(cx, cy, 1.4, P.GLOW_BLUE[0])
        img.set(8, 5, "#FFFFFF")
    return img


def crystal_flower():
    img = Img(16, 16)
    stem_line(img, [(8, 15), (8, 9)], P.GLADE_STEM)
    leaf(img, 8, 13, -1, -0.6, 3.5, P.MINT)
    leaf(img, 8, 12, 1, -0.6, 3.5, P.MINT)
    cols = [P.MAGENTA, P.LAVENDER, P.BLUE, P.PINK]
    shards = [(8, 2, 0, 6), (5, 5, -1, 4), (11, 5, 1, 4), (6, 8, -1, 2), (10, 8, 1, 2)]
    for i, (x, y, lean, length) in enumerate(shards):
        pal = cols[i % len(cols)]
        for k in range(length):
            px = x + round(lean * k * 0.3)
            py = y + k
            w = min(k, 1)
            for dx in range(-w, w + 1):
                img.set(px + dx, py, pal[4] if dx < 0 else (pal[2] if dx > 0 else pal[3]))
        img.set(x, y - 1, "#FFFFFF")
    img.set(8, 8, P.WHITE[4])
    return img


# --------------------------------------------------------------------------- tall flowers

def tall_lavender(top):
    img = Img(16, 16)
    spikes = ((4, -1), (8, 0), (12, 1))
    if top:
        for sx, bend in spikes:
            start = 1 + abs(bend)
            for y in range(start, 16):
                x = sx + (bend if y < 8 else 0)
                img.set(x, y, P.SAGE[1])
                if y < 12:
                    img.set(x - 1, y, P.LAVENDER[2 if y % 2 else 1])
                    img.set(x + 1, y, P.LAVENDER[3 if y % 2 else 2])
                    img.set(x, y, P.LAVENDER[3] if y % 3 else P.LAVENDER[2])
            img.set(sx + bend, start - 1, P.LAVENDER[4])
    else:
        for sx, bend in spikes:
            stem_line(img, [(sx, 15), (sx, 0)], P.SAGE)
            for y in range(0, 4):
                img.set(sx - 1, y, P.LAVENDER[1])
                img.set(sx + 1, y, P.LAVENDER[2])
        for lx, d in ((4, -1), (8, 1), (12, 1), (6, -1)):
            leaf(img, lx, 15, d, -1.3, 4, P.SAGE, 1.0)
    return img


def fairy_foxglove(top):
    img = Img(16, 16)
    if top:
        stem_line(img, [(8, 15), (8, 1)], P.STEM)
        bells = [(9, 2, 0), (7, 4, 1), (9, 6, 2), (7, 8, 3), (9, 10, 2), (7, 12, 1)]
        for x, y, size in bells:
            pal = P.PINK if y % 4 else P.LAVENDER
            side = 1 if x > 8 else -1
            for dy in range(size + 2):
                w = min(dy, 1 + size // 2)
                for dx in range(0, w + 1):
                    img.set(x + side * dx, y + dy, pal[3] if dx == 0 else pal[2])
            img.set(x + side, y + size + 1, P.WHITE[4])
        img.set(8, 0, P.PINK[3])
    else:
        stem_line(img, [(8, 15), (8, 0)], P.STEM)
        for lx, ly, d, ln in ((8, 15, -1, 6), (8, 15, 1, 6), (8, 12, -1, 4.5), (8, 11, 1, 4.5), (8, 7, -1, 3)):
            leaf(img, lx, ly, d, -0.6, ln, P.LEAF, 1.6)
        for x, y in ((9, 1), (7, 3)):
            img.set(x, y, P.PINK[2])
            img.set(x, y + 1, P.PINK[1])
    return img


# --------------------------------------------------------------------------- grasses & bushes

def short_grass(seed, base_pal, tip_pal, glow_tip=None):
    img = Img(16, 16)
    rng = Rng(seed)
    for i in range(11):
        x = rng.range(1, 14)
        h = rng.range(6, 13)
        lean = rng.uniform(-0.25, 0.25)
        for k in range(h):
            y = 15 - k
            px = round(x + lean * k)
            t = k / h
            if t > 0.65:
                c = ramp(tip_pal, 0.4 + (t - 0.65) * 1.6)
            else:
                c = ramp(base_pal, 0.3 + t * 0.6)
            img.set(px, y, c)
        if glow_tip:
            img.set(round(x + lean * (h - 1)), 15 - (h - 1), glow_tip)
    return img


def hydrangea(seed, flower_pal, top):
    img = Img(16, 16)
    rng = Rng(seed + ("t" if top else "s"))
    for y in range(16):
        for x in range(16):
            n = value_noise(x, y, 3, seed + "leaf")
            if not top and y < 2 and hash01(seed, "edge", x) < 0.5:
                continue
            img.set(x, y, ramp(P.LEAF, 0.25 + n * 0.55))
    clusters = 5 if top else 4
    for i in range(clusters):
        cx, cy = rng.range(2, 13), rng.range(2 if top else 3, 12)
        for k in range(14):
            fx = cx + rng.range(-2, 2)
            fy = cy + rng.range(-2, 2)
            for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
                shade = 3 if (dx + dy) == 0 else (2 if dx + dy == 1 else 1)
                img.set(fx + dx, fy + dy, flower_pal[shade])
            img.set(fx, fy, flower_pal[4] if rng.chance(0.3) else flower_pal[3])
    return img


# --------------------------------------------------------------------------- crops

def strawberry_stage(stage):
    img = Img(16, 16)
    if stage == 0:
        for x, h in ((5, 3), (8, 4), (11, 3)):
            stem_line(img, [(x, 15), (x, 15 - h)], P.STEM)
            leaf(img, x, 15 - h, -1, -0.4, 2, P.LEAF, 1)
            leaf(img, x, 15 - h, 1, -0.4, 2, P.LEAF, 1)
        return img
    rng = Rng("strawberry")
    top = 7 if stage == 1 else 5
    for i in range(9):
        x = rng.range(2, 13)
        y = rng.range(top, 13)
        stem_line(img, [(8, 15), (x, y)], P.STEM)
        for dx, dy in ((-1, -1), (1, -1), (0, -2)):
            leaf(img, x, y, dx, dy, 2.2, P.LEAF, 1.2)
    if stage >= 2:
        for x, y in ((4, 7), (11, 6), (8, 9)) if stage == 2 else ((4, 6),):
            floret(img, x, y, P.WHITE, P.BUTTER[2])
    if stage == 3:
        for x, y in ((5, 10), (10, 9), (8, 12), (12, 12)):
            img.set(x, y - 1, P.LEAF[3])
            img.set(x - 1, y - 1, P.LEAF[2])
            img.set(x + 1, y - 1, P.LEAF[2])
            for dy, w in ((0, 1), (1, 1), (2, 0)):
                for dx in range(-w, w + 1):
                    img.set(x + dx, y + dy, P.RED[3] if dx < 0 and dy == 0 else P.RED[2])
            img.set(x - 1, y + 1, P.RED[1])
            img.set(x, y, P.BUTTER[3])
            img.set(x + 1, y + 1, P.BUTTER[3])
    return img


def starbloom_stage(stage):
    img = Img(16, 16)
    heights = [4, 8, 12, 13][stage]
    for x in (3, 7, 12):
        top = 15 - heights + (x % 3)
        stem_line(img, [(x, 15), (x, top)], P.GLADE_STEM)
        if stage >= 1:
            leaf(img, x, 15 - heights // 2, -1, -0.5, 2.5, P.MINT, 1)
        if stage == 2:
            img.set(x, top - 1, P.LAVENDER[2])
            img.set(x - 1, top, P.LAVENDER[2])
            img.set(x + 1, top, P.LAVENDER[1])
            img.set(x, top - 2, P.LAVENDER[3])
        elif stage == 3:
            cy = top - 1
            for dx, dy in ((0, -2), (0, -1), (-1, 0), (-2, 0), (1, 0), (2, 0), (0, 1), (-1, 1), (1, 1), (-1, -1), (1, -1)):
                img.set(x + dx, cy + dy, P.CREAM[4] if abs(dx) + abs(dy) < 2 else P.PINK[3])
            img.set(x, cy, P.BUTTER[2])
    return img


# --------------------------------------------------------------------------- mushrooms

def fairy_mushroom():
    img = Img(16, 16)

    def shroom(cx, top, w, h, stem_h):
        img.rect(cx - 1, top + h, cx, top + h + stem_h, P.CREAM[3])
        img.rect(cx, top + h, cx, top + h + stem_h, P.CREAM[2])
        for y in range(h):
            half = int(w / 2 * math.sqrt(1 - ((h - 1 - y) / h) ** 2) + 0.5) if y < h - 1 else w // 2
            for x in range(cx - half - 1, cx + half + 1):
                img.set(x, top + y, P.PINK[3] if x < cx - 1 and y < h - 1 else P.PINK[2])
        img.rect(cx - w // 2 - 1, top + h - 1, cx + w // 2, top + h - 1, P.PINK[1])
        for dx, dy in ((-2, 1), (1, 0), (2, 2), (-1, 2)):
            if w >= 6 or abs(dx) < 2:
                img.set(cx + dx, top + dy, P.WHITE[4])

    shroom(9, 5, 7, 4, 6)
    shroom(5, 10, 4, 2, 3)
    return img


def glowcap():
    img = Img(16, 16)
    for cx, top, w, stem_h in ((8, 4, 6, 8), (4, 9, 4, 4), (12, 10, 3, 3)):
        img.rect(cx - 1, top + 2, cx - 1, top + 2 + stem_h, P.WHITE[3])
        img.rect(cx, top + 2, cx, top + 2 + stem_h, P.WHITE[2])
        for y in range(3):
            half = [w // 3, w // 2, w // 2][y]
            for x in range(cx - half - 1, cx + half + 1):
                img.set(x, top + y, P.GLOW_BLUE[1] if y < 2 else P.BLUE[2])
        img.set(cx - 1, top, P.GLOW_BLUE[2])
        img.set(cx + 1, top + 1, P.GLOW_BLUE[2])
    return img


def mushroom_cap_block(seed, pal, spot, glowing=False):
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            n = value_noise(x, y, 4, seed, wrap=4)
            img.set(x, y, ramp(pal, 0.4 + n * 0.35))
    rng = Rng(seed + "spots")
    for i in range(6):
        cx, cy = rng.range(1, 14), rng.range(1, 14)
        r = rng.uniform(1.0, 1.8)
        for y in range(cy - 2, cy + 3):
            for x in range(cx - 2, cx + 3):
                if (x - cx) ** 2 + (y - cy) ** 2 <= r * r:
                    img.set(x % 16, y % 16, spot if not glowing or (x + y) % 2 else lighten(spot, 0.4))
    return img


# --------------------------------------------------------------------------- hanging & water plants

def moonbell():
    img = Img(16, 16)
    stem_line(img, [(8, 0), (8, 4), (6, 7)], P.MOON_STEM)
    stem_line(img, [(8, 3), (10, 6), (10, 8)], P.MOON_STEM)
    leaf(img, 8, 1, -1, 0.4, 3, P.MOON_LEAF, 1)
    leaf(img, 8, 2, 1, 0.5, 3, P.MOON_LEAF, 1)

    def bell(x, y, s):
        for dy in range(s + 1):
            w = min(dy, (s + 1) // 2 + 1) if dy else 0
            for dx in range(-w, w + 1):
                img.set(x + dx, y + dy, P.GLOW_BLUE[2] if dx < 0 else P.GLOW_BLUE[1])
        for dx in range(-(s // 2 + 1), s // 2 + 2):
            img.set(x + dx, y + s + 1, P.GLOW_BLUE[0])
        img.set(x, y + s + 2, "#FFFFFF")

    bell(6, 8, 3)
    bell(10, 9, 2)
    return img


def lily_pad():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8
            d = math.hypot(dx, dy)
            ang = math.atan2(dy, dx)
            if d <= 7.2 and not (abs(ang - 0.6) < 0.18 and d > 1):
                t = 0.45 + (d / 7.2) * 0.25 + (hash01("pad", x, y) - 0.5) * 0.1
                if abs(math.sin(ang * 4)) < 0.12:
                    t -= 0.15
                img.set(x, y, ramp(P.MINT, t))
    return img


def lily_flower():
    img = Img(16, 16)
    cx = 8
    petals = [(-4, 13, 3), (4, 13, 3), (-2, 11, 4), (2, 11, 4), (0, 9, 5)]
    for ox, base, h in petals:
        for k in range(h):
            y = base - k + (2 if abs(ox) == 4 else 0)
            w = 1 if k < h - 1 else 0
            for dx in range(-w, w + 1):
                x = cx + ox + dx + (0 if ox == 0 else (1 if ox > 0 else -1) * (k // 3))
                img.set(x, y, P.PINK[3] if dx <= 0 else P.LAVENDER[2])
    img.rect(cx - 1, 12, cx + 1, 14, P.GLOW_PINK[2])
    img.set(cx, 11, "#FFFFFF")
    return img


def petals_bed(pal):
    img = Img(16, 16)
    rng = Rng("petals" + pal[2])
    for i in range(26):
        x, y = rng.range(0, 14), rng.range(0, 14)
        shade = rng.range(2, 4)
        img.set(x, y, pal[shade])
        img.set(x + 1, y, pal[shade - 1])
        if rng.chance(0.5):
            img.set(x, y + 1, pal[shade - 1])
    return img


def petals_stem():
    img = Img(16, 16)
    for y in range(16):
        img.set(0, y, P.STEM[2] if y < 6 else P.STEM[1])
    return img


def flower_carpet(seed, pal, center):
    img = Img(16, 16)
    rng = Rng(seed)
    for i in range(30):
        x, y = rng.range(0, 15), rng.range(0, 15)
        img.set(x, y, P.LEAF[rng.range(2, 4)])
    for i in range(16):
        cx, cy = rng.range(0, 15), rng.range(0, 15)
        for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
            img.set((cx + dx) % 16, (cy + dy) % 16, pal[3] if dy <= 0 else pal[2])
        img.set(cx, cy, center)
    return img
