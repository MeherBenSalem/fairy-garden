"""Entity, sign, boat, armour and particle textures.

Boats and signs must match vanilla's UV layouts, so vanilla's textures are read from the Minecraft jar in the
Gradle cache purely as *masks* (which pixels are used, and whether a pixel is wood or metal chain). All colours
and patterns are painted from scratch.
"""
import colorsys
import math
import zipfile
from pathlib import Path

from .core import Img, Rng, hash01, mix, ramp, value_noise, darken, lighten, with_alpha, load_png
from . import palette as P

CLIENT_JAR = Path.home() / ".gradle" / "caches" / "forge_gradle" / "minecraft_repo" / "versions" / "1.20.1" / "client-extra.jar"


def vanilla(path):
    if not CLIENT_JAR.exists():
        raise SystemExit("Minecraft client assets not found at %s; run './gradlew compileJava' once first." % CLIENT_JAR)
    with zipfile.ZipFile(CLIENT_JAR) as jar:
        return load_png(jar.read("assets/minecraft/textures/" + path))


def _saturation(c):
    return colorsys.rgb_to_hsv(c[0] / 255, c[1] / 255, c[2] / 255)[1]


def _plank_tone(x, y, seed):
    board = y // 4
    t = 0.55 + (hash01(seed, "b", board, x // 16) - 0.5) * 0.14 + (value_noise(x * 0.5, y * 2, 2, seed) - 0.5) * 0.16
    if y % 4 == 3:
        t -= 0.2
    return t


def paint_mask(mask, painter):
    """Repaint every opaque pixel of `mask` with painter(x, y, src_colour) and darken pixels on a mask edge."""
    out = Img(mask.w, mask.h)
    for y in range(mask.h):
        for x in range(mask.w):
            src = mask.get(x, y)
            if src[3] == 0:
                continue
            c = painter(x, y, src)
            if c is None:
                continue
            edge = any(mask.get(x + dx, y + dy)[3] == 0 for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            if edge:
                c = darken(c, 0.12)
            out.set(x, y, with_alpha(c, src[3]))
    return out


def boat(planks, seed, chest=False):
    mask = vanilla("entity/chest_boat/oak.png" if chest else "entity/boat/oak.png")

    def painter(x, y, src):
        if chest and y >= 56:
            # Chest parts: a deeper tone of the same wood so the chest stands out from the hull.
            return darken(ramp(planks, _plank_tone(x, y, seed)), 0.28)
        return ramp(planks, _plank_tone(x, y, seed))

    return paint_mask(mask, painter)


def sign(planks, post, seed):
    mask = vanilla("entity/signs/oak.png")

    def painter(x, y, src):
        if y >= 14 and x < 16:
            return ramp(post, 0.45 + (0.1 if x % 2 else 0))
        return ramp(planks, _plank_tone(x, y, seed))

    return paint_mask(mask, painter)


def hanging_sign(wood, seed):
    mask = vanilla("entity/signs/hanging/oak.png")

    def painter(x, y, src):
        if _saturation(src) < 0.18:
            return ramp(P.SILVER, 0.3 + (src[0] / 255) * 0.6)
        return ramp(wood, 0.45 + (value_noise(x, y * 0.4, 2, seed) - 0.5) * 0.25 + (0.08 if y % 5 == 0 else 0))

    return paint_mask(mask, painter)


def hanging_sign_gui(wood, seed):
    mask = vanilla("gui/hanging_signs/oak.png")

    def painter(x, y, src):
        if _saturation(src) < 0.18:
            return ramp(P.SILVER, 0.3 + (src[0] / 255) * 0.6)
        return ramp(wood, 0.5 + (value_noise(x, y * 0.4, 2, seed) - 0.5) * 0.25)

    return paint_mask(mask, painter)


# --------------------------------------------------------------------------------------------- creatures

def _wing_mask(x, z):
    """Left wing seen from above: 7 wide (0 = body), 8 deep (0 = front). Forewing + hindwing."""
    if z <= 4:
        return x <= 6 - max(0, 2 - z) * (1 if x > 4 else 0) and not (z == 0 and x > 4)
    return x <= 4 - (z - 5) and not (z == 7 and x == 0)


def butterfly(pal, body="#3A2E44"):
    img = Img(32, 32)
    img.rect(0, 0, 13, 6, body)
    for x, y in ((16, 0), (18, 0), (17, 1), (19, 0), (21, 0), (20, 1)):
        img.set(x, y, body)

    def wing(ox, oy, mirror, under):
        for z in range(8):
            for x in range(7):
                if not _wing_mask(x, z):
                    continue
                edge = not _wing_mask(x + 1, z) or not _wing_mask(x, z + 1) or not _wing_mask(x, z - 1) or x == 6
                t = 0.55 - x * 0.03
                c = ramp(pal, t + (0.25 if under else 0))
                if edge:
                    c = pal[1] if not under else pal[2]
                if x < 2 and not edge:
                    c = pal[3]
                if (x, z) in ((4, 2), (2, 6)):
                    c = "#FFFFFF"
                if (x, z) in ((3, 1), (5, 3)) and not under:
                    c = pal[4]
                px = ox + (6 - x if mirror else x)
                img.set(px, oy + z, c)

    wing(8, 8, False, False)
    wing(15, 8, False, True)
    wing(8, 16, True, False)
    wing(15, 16, True, True)
    return img


def moon_wisp():
    img = Img(32, 32)
    for y in range(0, 8):
        for x in range(0, 16):
            fx, fy = (x % 4) - 1.5, (y % 4) - 1.5
            d = math.hypot(fx, fy)
            img.set(x, y, mix("#FFFFFF", P.GLOW_BLUE[1], min(1, d / 2.2)))
    for y in range(8, 20):
        for x in range(0, 24):
            fx, fy = (x % 6) - 2.5, ((y - 8) % 6) - 2.5
            d = math.hypot(fx, fy)
            a = 70 + int(60 * (1 - min(1, d / 3.5)))
            c = with_alpha(mix(P.GLOW_BLUE[1], P.GLOW_VIOLET[1], hash01("halo", x, y)), a)
            if hash01("spark", x, y) < 0.06:
                c = with_alpha("#FFFFFF", 200)
            img.set(x, y, c)
    for oy in (20, 26):
        for y in range(oy, oy + 4):
            for x in range(4, 14):
                edge = y in (oy, oy + 3) or x in (4, 8, 9, 13)
                img.set(x, y, with_alpha(P.GLOW_VIOLET[2] if edge else P.GLOW_VIOLET[1], 210 if edge else 150))
    return img


def flower_crown_armor():
    """Helmet layer: a ring of flowers around the top of the head on the outer (hat) layer."""
    img = Img(64, 32)
    pals = [P.PINK, P.LAVENDER, P.CREAM, P.MINT, P.BLUE, P.WHITE]
    # Side faces of the hat layer: x 32..63, y 8..15. Crown band on the top three rows.
    for x in range(32, 64):
        img.set(x, 10, P.LEAF[2] if x % 2 else P.LEAF[3])
    k = 0
    for x in range(33, 64, 3):
        pal = pals[k % len(pals)]
        k += 1
        img.set(x, 9, pal[3])
        img.set(x - 1, 9, pal[2])
        img.set(x + 1, 9, pal[2])
        img.set(x, 8, pal[3])
        img.set(x, 10, pal[2])
        img.set(x, 9, P.BUTTER[2])
    # Top face (40..47, 0..7): flowers around the rim.
    for i in range(8):
        for (x, y) in ((40 + i, 0), (40 + i, 7), (40, i), (47, i)):
            img.set(x, y, P.LEAF[3])
    for i, (x, y) in enumerate(((41, 0), (44, 0), (47, 2), (47, 5), (45, 7), (42, 7), (40, 5), (40, 2))):
        img.set(x, y, pals[i % len(pals)][3])
    return img


# --------------------------------------------------------------------------------------------- particles

def petal_particle(pal, variant):
    img = Img(8, 8)
    ang = variant * math.pi / 4 + 0.3
    for y in range(8):
        for x in range(8):
            dx, dy = x + 0.5 - 4, y + 0.5 - 4
            u = dx * math.cos(ang) + dy * math.sin(ang)
            v = -dx * math.sin(ang) + dy * math.cos(ang)
            if (u / 2.9) ** 2 + (v / 1.7) ** 2 <= 1 and not (u > 2.2 and abs(v) < 0.6):
                img.set(x, y, pal[3] if v < 0 else pal[2])
    img.set(3, 3, pal[4])
    return img


def butterfly_particle(frame):
    img = Img(8, 8)
    spans = [3, 2, 1][frame]
    for y in range(2, 6):
        img.set(3, y, "#6E6478")
        img.set(4, y, "#6E6478")
    for side in (-1, 1):
        for k in range(1, spans + 1):
            x = (3 if side < 0 else 4) + side * k
            for y in range(1, 5):
                img.set(x, y, "#FFFFFF" if k < spans else "#E6E0EE")
            for y in range(4, 7):
                if k < spans or spans == 1:
                    img.set(x, y, "#F2EEF6")
    img.set(2, 0, "#6E6478")
    img.set(5, 0, "#6E6478")
    return img


def sparkle_particle(frame):
    """Four-point twinkle that grows and shrinks over the frames."""
    img = Img(8, 8)
    r = [1, 2, 3, 2][frame]
    img.rect(3, 3, 4, 4, "#FFFFFF")
    for k in range(1, r + 1):
        a = 255 if k < r else 170
        for (x, y) in ((3, 3 - k), (4, 4 + k), (3 - k, 4), (4 + k, 3)):
            img.set(x, y, with_alpha("#FFFFFF", a))
    if frame == 2:
        for x, y in ((2, 2), (5, 2), (2, 5), (5, 5)):
            img.set(x, y, with_alpha("#FFFFFF", 120))
    return img


def glimmer_particle(radius):
    img = Img(8, 8)
    for y in range(8):
        for x in range(8):
            d = math.hypot(x + 0.5 - 4, y + 0.5 - 4)
            if d <= radius:
                img.set(x, y, with_alpha("#FFFFFF", int(255 * (1 - (d / radius) ** 2) ** 0.8)))
    return img


def pollen_particle(big):
    img = Img(8, 8)
    img.set(3, 3, "#FFFFFF")
    img.set(4, 4, with_alpha("#FFFFFF", 200))
    if big:
        img.set(4, 3, "#FFFFFF")
        img.set(3, 4, "#FFFFFF")
        for x, y in ((2, 3), (5, 4), (3, 2), (4, 5)):
            img.set(x, y, with_alpha("#FFFFFF", 110))
    return img
