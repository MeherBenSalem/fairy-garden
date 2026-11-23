"""Wood family textures: bark, log ends, stripped logs, planks, doors and trapdoors."""
import math

from .core import Img, hash01, mix, ramp, value_noise, darken, lighten, C


def bark(seed, pal, glow=None):
    """Vertical bark with wandering grooves. Returns (bark, glow_overlay_or_None). Tiles in both directions."""
    img = Img(16, 16)
    grooves = [1 + hash01(seed, "g", i) * 2 + i * 4 for i in range(4)]
    phases = [hash01(seed, "p", i) * math.tau for i in range(4)]
    for y in range(16):
        for x in range(16):
            n = value_noise(x, y, 4, seed, wrap=4)
            t = 0.45 + (n - 0.5) * 0.45
            for g, ph in zip(grooves, phases):
                off = math.sin(y / 16 * math.tau + ph) * 0.9
                d = abs(((x - g - off + 8) % 16) - 8)
                if d < 0.7:
                    t -= 0.32
                elif d < 1.5:
                    t += 0.12
            if hash01(seed, "fleck", x, y) < 0.05:
                t += 0.15
            img.set(x, y, ramp(pal, t))
    overlay = None
    if glow:
        overlay = Img(16, 16)
        for g, ph in zip(grooves[::2], phases[::2]):
            for y in range(16):
                off = math.sin(y / 16 * math.tau + ph) * 0.9
                x = int(round(g + off)) % 16
                if hash01(seed, "vein", y, int(g)) < 0.55:
                    overlay.set(x, y, glow[1] if hash01(seed, "vb", x, y) < 0.7 else glow[2])
        for i in range(5):
            overlay.set(int(hash01(seed, "sx", i) * 16), int(hash01(seed, "sy", i) * 16), glow[2])
    return img, overlay


def log_top(seed, bark_pal, wood_pal, glow=None):
    """Log cross-section: bark rim and concentric growth rings."""
    img = Img(16, 16)
    overlay = Img(16, 16) if glow else None
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                img.set(x, y, ramp(bark_pal, 0.35 + hash01(seed, "rim", x, y) * 0.3))
                continue
            dx, dy = x - 7.5, y - 7.5
            d = math.sqrt(dx * dx + dy * dy) * 0.7 + max(abs(dx), abs(dy)) * 0.35
            ring = int(d) % 2
            t = 0.55 + (0.12 if ring else -0.05) + (value_noise(x, y, 3, seed) - 0.5) * 0.15
            if d < 1.2:
                t = 0.3
            img.set(x, y, ramp(wood_pal, t))
            if overlay is not None and int(d) == 4 and hash01(seed, "gr", x, y) < 0.6:
                overlay.set(x, y, glow[1])
    return img, overlay


def stripped(seed, pal):
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            n = value_noise(x, y * 0.35, 2, seed, wrap=8)
            t = 0.55 + (n - 0.5) * 0.35
            if (x + int(hash01(seed, "f", y // 4) * 3)) % 5 == 0:
                t -= 0.12
            img.set(x, y, ramp(pal, t))
    return img


def stripped_top(seed, pal):
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            d = math.sqrt(dx * dx + dy * dy) * 0.7 + max(abs(dx), abs(dy)) * 0.35
            t = 0.6 + (0.1 if int(d) % 2 else -0.04)
            if x in (0, 15) or y in (0, 15):
                t = 0.42
            img.set(x, y, ramp(pal, t))
    return img


def planks(seed, pal):
    img = Img(16, 16)
    for board in range(4):
        tone = (hash01(seed, "board", board) - 0.5) * 0.12
        seam = int(hash01(seed, "seam", board) * 16)
        for y in range(board * 4, board * 4 + 4):
            for x in range(16):
                grain = value_noise(x * 0.5, y * 2, 2, seed + str(board))
                t = 0.58 + tone + (grain - 0.5) * 0.18
                if y == board * 4 + 3:
                    t = 0.22
                elif y == board * 4:
                    t += 0.08
                if x == seam and y != board * 4 + 3:
                    t = 0.3
                img.set(x, y, ramp(pal, t))
    return img


def door(seed, pal, bark_pal, top, motif):
    """Door half. motif: 'flower' or 'moon' window in the top half."""
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            plank = x // 4
            t = 0.55 + (hash01(seed, "dp", plank) - 0.5) * 0.12 + (value_noise(x, y * 2, 3, seed) - 0.5) * 0.12
            if x % 4 == 3:
                t -= 0.16
            img.set(x, y, ramp(pal, t))
    frame = ramp(pal, 0.2)
    for i in range(16):
        img.set(0, i, frame)
        img.set(15, i, frame)
        img.set(i, 0 if top else 15, frame)
    if top:
        # Window with a decorative cut-out; the transparent pixels are see-through glass gaps.
        img.rect(3, 2, 12, 11, ramp(pal, 0.25))
        for y in range(3, 11):
            for x in range(4, 12):
                cx, cy = x - 7.5, y - 6.5
                if motif == "flower":
                    ang = math.atan2(cy, cx)
                    r = math.hypot(cx, cy)
                    petal = 2.6 + 1.3 * math.cos(ang * 4)
                    hole = r < petal or r < 1.0
                    if r < 1.2:
                        hole = False
                else:
                    hole = math.hypot(cx, cy) < 3.4 and math.hypot(cx - 1.6, cy - 1.0) > 2.6
                img.set(x, y, (0, 0, 0, 0) if hole else ramp(pal, 0.45))
        if motif == "flower":
            img.set(7, 6, "#FFE9A0")
            img.set(8, 7, "#FFE9A0")
        img.rect(1, 14, 14, 14, ramp(pal, 0.3))
    else:
        for (x0, y0, x1, y1) in ((2, 2, 7, 12), (8, 2, 13, 12)):
            for y in range(y0, y1 + 1):
                for x in range(x0, x1 + 1):
                    edge = x in (x0, x1) or y in (y0, y1)
                    if edge:
                        img.set(x, y, ramp(pal, 0.32 if (x == x0 or y == y0) else 0.72))
        gold = ["#C08A2A", "#FFE08A"]
        img.set(12, 0 + 1, gold[1])
        img.set(12, 2, gold[0])
    return img


def trapdoor(seed, pal, motif):
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            t = 0.55 + (value_noise(x * 2, y, 3, seed) - 0.5) * 0.14
            if y % 4 == 3:
                t -= 0.14
            img.set(x, y, ramp(pal, t))
    frame = ramp(pal, 0.22)
    for i in range(16):
        for j in (0, 1, 14, 15):
            img.set(i, j, frame if j in (0, 15) else ramp(pal, 0.35))
            img.set(j, i, frame if j in (0, 15) else ramp(pal, 0.35))
    for y in range(3, 13):
        for x in range(3, 13):
            cx, cy = x - 7.5, y - 7.5
            if motif == "flower":
                r = math.hypot(cx, cy)
                ang = math.atan2(cy, cx)
                hole = r < 2.8 + 1.4 * math.cos(ang * 4) and r > 1.3
            else:
                hole = (abs(cx) < 4.6 and abs(cy) < 4.6) and ((int(x) + int(y)) % 4 in (0, 1)) and not (x in (7, 8) or y in (7, 8))
            if hole:
                img.set(x, y, (0, 0, 0, 0))
    return img


def door_item(top_img, bottom_img):
    """A 16x16 inventory icon squeezed from the two door halves."""
    img = Img(16, 16)
    for y in range(16):
        src = top_img if y < 8 else bottom_img
        sy = (y % 8) * 2
        for x in range(4, 12):
            sx = (x - 4) * 2
            c = src.get(sx, sy)
            if c[3] == 0:
                c = (0, 0, 0, 0)
            img.set(x, y, c)
    return img
