#!/usr/bin/env python3
"""Generates every Fairy Garden texture (blocks, items, entities, particles, GUI, armour and the mod logo).

    python tools/generate_textures.py

Everything is painted procedurally from code, deterministically, so re-running gives identical files.
Vanilla textures are only read (from the Gradle cache) as UV-layout masks for boats and signs.
Run it before `gradlew runData`, which validates that every referenced texture exists.
"""
import shutil
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from texgen import decor, entities, items, plants, wood  # noqa: E402
from texgen import palette as P  # noqa: E402
from texgen.core import RES, TEX, Img, mix, ramp, with_alpha, darken, lighten, write_text  # noqa: E402


def block(img, name):
    img.save(TEX / "block" / (name + ".png"))


def item(img, name):
    img.save(TEX / "item" / (name + ".png"))


def woods():
    sets = {
        "blossom": dict(bark=P.BLOSSOM_BARK, wood=P.BLOSSOM_WOOD, motif="flower", glow=None),
        "moonwood": dict(bark=P.MOON_BARK, wood=P.MOON_WOOD, motif="moon", glow=P.GLOW_BLUE),
    }
    for name, s in sets.items():
        bark, glow = wood.bark(name, s["bark"], s["glow"])
        block(bark, name + "_log")
        top, top_glow = wood.log_top(name, s["bark"], s["wood"], s["glow"])
        block(top, name + "_log_top")
        if glow:
            block(glow, name + "_log_glow")
            block(top_glow, name + "_log_top_glow")
        block(wood.stripped(name + "s", s["wood"]), "stripped_" + name + "_log")
        block(wood.stripped_top(name + "s", s["wood"]), "stripped_" + name + "_log_top")
        block(wood.planks(name + "p", s["wood"]), name + "_planks")
        door_top = wood.door(name + "d", s["wood"], s["bark"], True, s["motif"])
        door_bottom = wood.door(name + "d", s["wood"], s["bark"], False, s["motif"])
        block(door_top, name + "_door_top")
        block(door_bottom, name + "_door_bottom")
        item(wood.door_item(door_top, door_bottom), name + "_door")
        block(wood.trapdoor(name + "t", s["wood"], s["motif"]), name + "_trapdoor")

        item(items.sign_item(s["wood"], s["bark"]), name + "_sign")
        item(items.hanging_sign_item(s["wood"]), name + "_hanging_sign")
        item(items.boat_item(s["wood"]), name + "_boat")
        item(items.boat_item(s["wood"], chest=True), name + "_chest_boat")
        entities.boat(s["wood"], name).save(TEX / "entity" / "boat" / (name + ".png"))
        entities.boat(s["wood"], name, chest=True).save(TEX / "entity" / "chest_boat" / (name + ".png"))
        entities.sign(s["wood"], s["bark"], name).save(TEX / "entity" / "signs" / (name + ".png"))
        entities.hanging_sign(s["wood"], name).save(TEX / "entity" / "signs" / "hanging" / (name + ".png"))
        entities.hanging_sign_gui(s["wood"], name).save(TEX / "gui" / "hanging_signs" / (name + ".png"))


def foliage():
    leaves, _ = plants.leaves("blossom_leaves", ["#C9628F", "#E07FA6", "#F29BBF", "#FFBCD5", "#FFDDEA"], P.PINK, 26, P.BUTTER[3],
                              accents=P.LEAF)
    block(leaves, "blossom_leaves")
    leaves, _ = plants.leaves("snow_blossom_leaves", ["#C9C2D6", "#DCD6E6", "#EEEAF4", "#F8F6FB", "#FFFFFF"], P.WHITE, 26, P.CREAM[2],
                              accents=P.MINT)
    block(leaves, "snow_blossom_leaves")
    leaves, glow = plants.leaves("moonwood_leaves", P.MOON_LEAF, P.LAVENDER, 4, P.GLOW_BLUE[2], glow=P.GLOW_BLUE)
    block(leaves, "moonwood_leaves")
    block(glow, "moonwood_leaves_glow")
    block(plants.sapling("blossom_sapling", P.BLOSSOM_BARK, P.PINK, P.WHITE), "blossom_sapling")
    block(plants.sapling("snow_blossom_sapling", P.BLOSSOM_BARK, P.WHITE, P.PINK), "snow_blossom_sapling")
    block(plants.sapling("moonwood_sapling", P.MOON_BARK, P.MOON_LEAF, P.GLOW_BLUE), "moonwood_sapling")


def flowers():
    block(plants.rosy_daisy(), "rosy_daisy")
    block(plants.lavender_plant(), "lavender")
    block(plants.cream_buttercup(), "cream_buttercup")
    block(plants.mint_posy(), "mint_posy")
    block(plants.starflower(), "starflower")
    block(plants.bluebell(), "bluebell")
    block(plants.moonflower(False), "moonflower")
    block(plants.moonflower(True), "moonflower_open")
    block(plants.crystal_flower(), "crystal_flower")
    block(plants.tall_lavender(False), "tall_lavender_bottom")
    block(plants.tall_lavender(True), "tall_lavender_top")
    block(plants.fairy_foxglove(False), "fairy_foxglove_bottom")
    block(plants.fairy_foxglove(True), "fairy_foxglove_top")
    block(plants.short_grass("blush_grass", P.LEAF, P.PINK), "blush_grass")
    block(plants.short_grass("moonlit_grass", P.MOON_LEAF, P.GLOW_BLUE, P.GLOW_BLUE[2]), "moonlit_grass")
    for color, pal in (("pink", P.PINK), ("blue", P.BLUE), ("lavender", P.LAVENDER)):
        block(plants.hydrangea(color, pal, False), color + "_hydrangea")
        block(plants.hydrangea(color, pal, True), color + "_hydrangea_top")
    for stage in range(4):
        block(plants.strawberry_stage(stage), "strawberry_bush_stage%d" % stage)
        block(plants.starbloom_stage(stage), "starbloom_stage%d" % stage)
    block(plants.fairy_mushroom(), "fairy_mushroom")
    block(plants.glowcap(), "glowcap")
    block(plants.mushroom_cap_block("fairy_mushroom_block", P.PINK, P.WHITE[4]), "fairy_mushroom_block")
    block(plants.mushroom_cap_block("glowcap_block", P.BLUE, P.GLOW_BLUE[2], glowing=True), "glowcap_block")
    block(plants.moonbell(), "moonbell")
    pad, flower = plants.lily_pad(), plants.lily_flower()
    block(pad, "glimmer_lily")
    block(flower, "glimmer_lily_flower")
    item(items.glimmer_lily_item(pad, flower), "glimmer_lily")
    for name, pal in (("blossom_petals", P.PINK), ("snow_blossom_petals", P.WHITE)):
        block(plants.petals_bed(pal), name)
        block(plants.petals_stem(), name + "_stem")
        item(items.petals_item(pal), name)
    for name, pal, center in (("pink", P.PINK, P.BUTTER[2]), ("lavender", P.LAVENDER, P.WHITE[4]), ("cream", P.CREAM, P.BUTTER[1]),
                              ("mint", P.MINT, P.WHITE[4]), ("blue", P.BLUE, P.WHITE[4])):
        block(plants.flower_carpet(name + "_carpet", pal, center), name + "_flower_carpet")


def decorations():
    block(decor.giant_stem(False), "giant_flower_stem")
    block(decor.giant_stem(True), "giant_flower_stem_top")
    for name, pal in (("pink", P.PINK), ("lavender", P.LAVENDER), ("blue", P.BLUE), ("cream", P.CREAM)):
        block(decor.giant_petals(name, pal), name + "_giant_petals")
    block(decor.pistil(), "giant_flower_pistil")
    block(decor.crystal_block(), "fairy_crystal_block")
    block(decor.crystal_cluster(), "fairy_crystal_cluster")
    for name, metal, glow in (("fairy_lantern", P.GOLD, P.GLOW_PINK), ("moon_lantern", P.SILVER, P.GLOW_BLUE)):
        block(decor.lantern(metal, glow), name)
        write_text("block/%s.png.mcmeta" % name, '{\n  "animation": {\n    "frametime": 8,\n    "interpolate": true\n  }\n}\n')
        item(decor.lantern_item(metal, glow), name)
    block(decor.lavender_bale(False), "lavender_bale_side")
    block(decor.lavender_bale(True), "lavender_bale_top")


def misc_items():
    item(items.strawberry(), "strawberry")
    item(items.shortcake(), "strawberry_shortcake")
    item(items.lavender_tea(), "lavender_tea")
    item(items.starbloom_seeds(), "starbloom_seeds")
    item(items.moon_petal(), "moon_petal")
    item(items.fairy_dust(), "fairy_dust")
    item(items.crystal_shard(), "crystal_shard")
    item(items.flower_crown(), "flower_crown")
    entities.flower_crown_armor().save(TEX / "models" / "armor" / "flower_crown_layer_1.png")


def creatures():
    for name, pal in (("pink", P.PINK), ("lavender", P.LAVENDER), ("cream", P.CREAM), ("mint", P.MINT),
                      ("sky", P.BLUE), ("white", P.WHITE)):
        entities.butterfly(pal).save(TEX / "entity" / "butterfly" / (name + ".png"))
    entities.moon_wisp().save(TEX / "entity" / "moon_wisp.png")


def particles():
    out = TEX / "particle"
    for i in range(4):
        entities.petal_particle(P.PINK, i).save(out / ("blossom_petal_%d.png" % i))
        entities.petal_particle(P.WHITE, i).save(out / ("snow_petal_%d.png" % i))
        entities.sparkle_particle(i).save(out / ("sparkle_%d.png" % i))
    for i in range(3):
        entities.butterfly_particle(i).save(out / ("butterfly_%d.png" % i))
    entities.glimmer_particle(2.6).save(out / "glimmer_0.png")
    entities.glimmer_particle(3.4).save(out / "glimmer_1.png")
    entities.pollen_particle(False).save(out / "pollen_0.png")
    entities.pollen_particle(True).save(out / "pollen_1.png")


# 5x7 bitmap glyphs for the logo title.
FONT = {
    "F": ["#####", "#....", "#....", "####.", "#....", "#....", "#...."],
    "A": [".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#"],
    "I": ["#####", "..#..", "..#..", "..#..", "..#..", "..#..", "#####"],
    "R": ["####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#"],
    "Y": ["#...#", "#...#", ".#.#.", "..#..", "..#..", "..#..", "..#.."],
    "G": [".###.", "#...#", "#....", "#.###", "#...#", "#...#", ".###."],
    "D": ["####.", "#...#", "#...#", "#...#", "#...#", "#...#", "####."],
    "E": ["#####", "#....", "#....", "####.", "#....", "#....", "#####"],
    "N": ["#...#", "##..#", "#.#.#", "#..##", "#...#", "#...#", "#...#"],
    " ": [".....", ".....", ".....", ".....", ".....", ".....", "....."],
}


def logo():
    import math
    w, h = 256, 128
    img = Img(w, h)
    for y in range(h):
        for x in range(w):
            sky = mix(P.LAVENDER[3], P.PINK[4], y / h)
            img.set(x, y, sky)
    img.disc(212, 26, 13, P.WHITE[4])
    img.disc(218, 22, 11, mix(P.LAVENDER[3], P.PINK[4], 22 / h))
    for i in range(40):
        x = int(((i * 73) % w))
        y = int((i * 37) % 60)
        img.set(x, y, "#FFFFFF")
    for layer, (base, pal) in enumerate(((86, P.MINT), (98, P.LEAF), (110, P.SAGE))):
        for x in range(w):
            top = base + int(8 * math.sin(x / (40 - layer * 6) + layer)) + int(4 * math.sin(x / 13.0 + layer * 2))
            for y in range(top, h):
                img.set(x, y, ramp(pal, 0.45 + layer * 0.1 + (y - top) / 200))
    for tx, ty in ((30, 92), (70, 100), (190, 96)):
        img.rect(tx, ty - 18, tx + 2, ty, P.BLOSSOM_BARK[2])
        img.disc(tx + 1, ty - 22, 11, P.PINK[3])
        img.disc(tx - 5, ty - 18, 7, P.PINK[2])
        img.disc(tx + 8, ty - 17, 7, P.PINK[4])
    for i in range(60):
        x = (i * 41) % w
        y = 104 + (i * 17) % 22
        img.set(x, y, [P.PINK[3], P.LAVENDER[3], P.CREAM[4], P.BLUE[3], "#FFFFFF"][i % 5])
    text = "FAIRY GARDEN"
    scale = 3
    width = len(text) * 6 * scale
    ox, oy = (w - width) // 2, 40
    for pass_ in (0, 1):
        for i, ch in enumerate(text):
            glyph = FONT[ch]
            for gy, row in enumerate(glyph):
                for gx, bit in enumerate(row):
                    if bit != "#":
                        continue
                    for sy in range(scale):
                        for sx in range(scale):
                            x = ox + (i * 6 + gx) * scale + sx
                            y = oy + gy * scale + sy
                            if pass_ == 0:
                                for dx, dy in ((-2, 0), (2, 0), (0, -2), (0, 2), (2, 2)):
                                    img.set(x + dx, y + dy, P.LAVENDER[0])
                            else:
                                img.set(x, y, mix("#FFFFFF", P.PINK[3], (gy * scale + sy) / (7 * scale)))
    img.save(RES / "fairygarden_logo.png")


def main():
    for sub in ("block", "item", "entity", "particle", "gui", "models"):
        shutil.rmtree(TEX / sub, ignore_errors=True)
    woods()
    foliage()
    flowers()
    decorations()
    misc_items()
    creatures()
    particles()
    logo()
    count = sum(1 for _ in TEX.rglob("*.png"))
    print("Wrote %d textures to %s" % (count, TEX))


if __name__ == "__main__":
    main()
