"""Inventory icons (16x16)."""
import math

from .core import Img, Rng, hash01, mix, ramp, lighten, darken, with_alpha
from . import palette as P
from .plants import floret, shaded_disc


def sign_item(planks, post):
    img = Img(16, 16)
    for y in range(2, 10):
        for x in range(1, 15):
            border = x in (1, 14) or y in (2, 9)
            img.set(x, y, ramp(planks, 0.22 if border else 0.55 + (0.1 if y % 3 == 0 else 0)))
    for y in range(4, 8):
        if y != 6:
            img.rect(4, y, 11, y, ramp(planks, 0.35))
    img.rect(7, 10, 8, 15, post[2])
    img.rect(7, 10, 7, 15, post[1])
    return img


def hanging_sign_item(wood):
    img = Img(16, 16)
    for x in (3, 12):
        for y in range(0, 5):
            img.set(x, y, P.SILVER[3] if y % 2 else P.SILVER[1])
    img.rect(2, 0, 13, 0, P.SILVER[2])
    for y in range(5, 13):
        for x in range(1, 15):
            border = x in (1, 14) or y in (5, 12)
            img.set(x, y, ramp(wood, 0.25 if border else 0.6 + (0.08 if (x + y) % 5 == 0 else 0)))
    img.rect(4, 8, 11, 8, ramp(wood, 0.35))
    img.rect(5, 10, 10, 10, ramp(wood, 0.35))
    return img


def boat_item(planks, chest=False):
    img = Img(16, 16)
    for y in range(7, 13):
        inset = (y - 7) // 2
        for x in range(1 + inset, 15 - inset):
            edge = y == 7 or x in (1 + inset, 14 - inset) or y == 12
            img.set(x, y, ramp(planks, 0.25 if edge else 0.55 + (0.1 if y == 9 else 0)))
    img.rect(3, 8, 12, 8, ramp(planks, 0.15))
    img.line(10, 3, 13, 9, P.CREAM[1])
    img.set(13, 10, P.CREAM[0])
    if chest:
        img.rect(4, 3, 9, 7, P.CREAM[0])
        img.rect(5, 4, 8, 6, darken(P.CREAM[0], 0.15))
        img.rect(4, 5, 9, 5, darken(P.CREAM[0], 0.35))
        img.set(6, 5, P.GOLD[3])
        img.set(7, 5, P.GOLD[3])
    return img


def strawberry():
    img = Img(16, 16)
    rows = [(5, 10), (4, 11), (4, 11), (4, 11), (5, 10), (5, 10), (6, 9), (7, 8)]
    for i, (a, b) in enumerate(rows):
        for x in range(a, b + 1):
            t = 0.62 - (x - 4) * 0.035 - i * 0.03
            img.set(x, 5 + i, ramp(P.RED, t))
    for x, y in ((5, 6), (8, 6), (10, 7), (6, 8), (9, 9), (7, 10), (5, 9), (8, 11)):
        img.set(x, y, P.BUTTER[3])
    img.set(5, 5, P.RED[4])
    img.set(6, 6, P.RED[4])
    for x, y in ((5, 4), (6, 3), (7, 4), (8, 3), (9, 4), (10, 4), (7, 2), (8, 2)):
        img.set(x, y, P.LEAF[3] if y < 4 else P.LEAF[2])
    img.set(8, 1, P.STEM[1])
    img.outline(darken(P.RED[0], 0.3))
    return img


def shortcake():
    img = Img(16, 16)
    for y in range(6, 14):
        for x in range(2, 2 + (y - 4) * 1 + 6):
            if x > 14:
                continue
            layer = (y - 6)
            c = P.CREAM[4] if layer in (0, 3) else (P.PINK[3] if layer == 4 else P.CREAM[3] if layer < 6 else P.BUTTER[2])
            img.set(x, y, c)
    img.rect(2, 13, 14, 13, P.BUTTER[1])
    img.disc(5, 4.5, 1.8, P.RED[2])
    img.set(4, 3, P.RED[4])
    img.set(5, 2, P.LEAF[3])
    img.outline(darken(P.CREAM[0], 0.4))
    return img


def lavender_tea():
    img = Img(16, 16)
    glass = with_alpha("#E8F4FF", 200)
    for y in range(6, 15):
        w = 3 if y < 8 else 4
        for x in range(8 - w, 8 + w):
            img.set(x, y, glass)
    img.rect(6, 2, 9, 5, glass)
    img.rect(6, 1, 9, 1, P.CREAM[1])
    for y in range(9, 14):
        for x in range(5, 11):
            img.set(x, y, ramp(P.LAVENDER, 0.45 + (x - 5) * 0.04))
    img.set(6, 10, P.LAVENDER[4])
    img.line(11, 3, 13, 8, P.SAGE[2])
    for y, x in ((3, 12), (4, 12), (5, 13), (6, 13)):
        img.set(x, y, P.LAVENDER[2])
    img.outline(with_alpha("#6C7CA0", 230))
    return img


def starbloom_seeds():
    img = Img(16, 16)
    for cx, cy, pal in ((5, 6, P.CREAM), (10, 5, P.LAVENDER), (8, 10, P.CREAM), (4, 11, P.LAVENDER), (12, 11, P.CREAM)):
        for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
            img.set(cx + dx, cy + dy, pal[2])
        img.set(cx, cy, pal[4])
    img.outline(with_alpha(P.LAVENDER[0], 200))
    return img


def moon_petal():
    """A single curved teardrop petal with a moonlit sheen, tip pointing up-right."""
    img = Img(16, 16)
    ang = -math.pi / 4
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8.5
            u = dx * math.cos(ang) - dy * math.sin(ang)   # along the petal
            v = dx * math.sin(ang) + dy * math.cos(ang)   # across the petal
            t = (u + 6.5) / 13.0
            if 0 <= t <= 1:
                half = 3.2 * math.sin(math.pi * t ** 0.8) * (1 - t * 0.35)
                bend = 0.9 * math.sin(math.pi * t)
                if abs(v - bend) <= half:
                    shade = 0.25 + t * 0.5 - (v - bend) / (half + 0.01) * 0.18
                    img.set(x, y, ramp([P.BLUE[2], P.GLOW_BLUE[1], P.GLOW_BLUE[2], "#FFFFFF"], shade))
    for x, y in ((6, 9), (7, 8), (8, 7), (9, 6)):
        img.set(x, y, P.WHITE[3])
    img.outline(with_alpha(P.BLUE[1], 230))
    return img


def fairy_dust():
    img = Img(16, 16)
    rng = Rng("dust")
    for y in range(9, 15):
        half = (y - 8) * 1.1
        for x in range(int(8 - half), int(8 + half) + 1):
            img.set(x, y, rng.pick([P.PINK[3], P.LAVENDER[3], P.PINK[2], P.MINT[3], P.BLUE[3]]))
    for x, y in ((8, 2), (4, 5), (12, 4), (10, 7)):
        img.set(x, y, "#FFFFFF")
        img.set(x - 1, y, with_alpha(P.PINK[3], 200))
        img.set(x + 1, y, with_alpha(P.PINK[3], 200))
        img.set(x, y - 1, with_alpha(P.LAVENDER[3], 200))
        img.set(x, y + 1, with_alpha(P.LAVENDER[3], 200))
    return img


def crystal_shard():
    img = Img(16, 16)
    for i in range(11):
        x, y = 3 + i, 13 - i
        w = 1 if 2 < i < 9 else 0
        for d in range(-w - 1, w + 2):
            pal = P.LAVENDER if d < 0 else P.PINK
            img.set(x + d, y, pal[4] if d == -w - 1 else pal[2 if d > 0 else 3])
    img.set(13, 3, "#FFFFFF")
    img.outline(with_alpha(P.MAGENTA[0], 220))
    return img


def flower_crown():
    img = Img(16, 16)
    for i in range(64):
        a = i / 64 * math.tau
        x = 8 + math.cos(a) * 6.2
        y = 9 + math.sin(a) * 3.4
        img.set(round(x), round(y), P.LEAF[2] if math.sin(a) > 0 else P.LEAF[1])
    pals = [P.PINK, P.LAVENDER, P.CREAM, P.MINT, P.BLUE, P.WHITE]
    for k in range(8):
        a = k / 8 * math.tau + 0.3
        x = round(8 + math.cos(a) * 6.2)
        y = round(9 + math.sin(a) * 3.4)
        floret(img, x, y, pals[k % len(pals)], P.BUTTER[2])
    return img


def glimmer_lily_item(pad, flower):
    img = pad.copy()
    for y in range(16):
        for x in range(16):
            c = flower.get(x, y)
            if c[3] and y > 6:
                img.set(x, y - 4, c)
    return img


def petals_item(pal):
    img = Img(16, 16)
    rng = Rng("petal-item" + pal[2])
    for cx, cy in ((5, 5), (10, 6), (7, 10), (11, 11), (4, 11)):
        for dx, dy in ((0, 0), (1, 0), (0, 1), (-1, 0), (0, -1)):
            img.set(cx + dx, cy + dy, pal[3] if dy <= 0 and dx <= 0 else pal[2])
        img.set(cx, cy, pal[4])
    img.outline(with_alpha(pal[0], 210))
    return img
