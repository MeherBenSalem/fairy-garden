#!/usr/bin/env python3
"""Renders the blocks and items the artwork shows from the mod's own models and textures, with
transparent backgrounds, into marketing/source/renders/. Needs Pillow and NumPy, no Minecraft.
Run from the repository root:

    python marketing/source/renders.py

- Full blocks (logs, planks, leaves, crystals, giant petals, mushroom blocks, the lavender bale)
  are drawn as isometric cubes from the textures their block model names, with the glowing
  overlay of moonwood logs on top, and the left and right faces shaded like Minecraft's
  inventory renders.
- Everything else (flowers, saplings, plants, lanterns, food, boats, signs, doors) is drawn the
  way Minecraft draws it in the inventory: the item model's flat texture, scaled up pixel-sharp.
- A few block states the cards need are drawn from their block textures: each starbloom and
  strawberry bush stage and the open moonflower.
"""
import json
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS_MAIN = ROOT / "src/main/resources/assets/fairygarden"
ASSETS_GEN = ROOT / "src/generated/resources/assets/fairygarden"
OUT = Path(__file__).resolve().parent / "renders"
SPRITE_SCALE = 16   # 16x16 texture -> 256x256
CUBE_SCALE = 12     # 16-texel cube -> about 333x384


def texture(ref):
    """'fairygarden:block/x' -> RGBA image (the first frame of animated textures)."""
    path = ref.split(":", 1)[-1]
    img = Image.open(ASSETS_MAIN / "textures" / f"{path}.png").convert("RGBA")
    if img.height > img.width:
        img = img.crop((0, 0, img.width, img.width))
    return img


def model(kind, name):
    for base in (ASSETS_GEN, ASSETS_MAIN):
        f = base / "models" / kind / f"{name}.json"
        if f.exists():
            return json.loads(f.read_text(encoding="utf-8"))
    raise FileNotFoundError(f"{kind}/{name}")


def sprite(img, scale=SPRITE_SCALE):
    return img.resize((img.width * scale, img.height * scale), Image.NEAREST)


def face(tex, n, coeffs, size):
    """Maps a texture onto a parallelogram with an inverse affine transform (output -> texel)."""
    tex = tex.resize((n, n), Image.NEAREST)
    return tex.transform(size, Image.AFFINE, coeffs, resample=Image.NEAREST, fillcolor=(0, 0, 0, 0))


def shade(img, factor):
    a = np.asarray(img).astype(np.float32)
    a[..., :3] *= factor
    return Image.fromarray(a.clip(0, 255).astype(np.uint8), "RGBA")


def cube(top, side, overlay_top=None, overlay_side=None, s=CUBE_SCALE):
    """An isometric cube: top face, and the two front faces shaded 0.8 and 0.6 like the inventory."""
    n = 16
    a = s * 0.8660254
    b = s * 0.5
    w = int(round(2 * n * a)) + 2
    h = int(round(2 * n * s)) + 2
    cx = w / 2
    cy = n * s + 1  # screen y of the cube's top-back corner at ground level (u=0, v=0, h=0) is cy; top face is n*s higher
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))

    # Top face (h = n): X = cx + (tu - tv)a, Y = cy + (tu + tv)b - n*s
    # tu = (X-cx)/(2a) + (Y-cy+n*s)/(2b), tv = (Y-cy+n*s)/(2b) - (X-cx)/(2a)
    y0 = cy - n * s
    top_c = (1 / (2 * a), 1 / (2 * b), -cx / (2 * a) - y0 / (2 * b),
             -1 / (2 * a), 1 / (2 * b), cx / (2 * a) - y0 / (2 * b))
    # Left face (v = n): X = cx + (tu - n)a, Y = cy + (tu + n)b - (n - tv)s
    # tu = (X-cx)/a + n ; tv = (Y - cy - (tu+n)b)/s + n = (Y - cy)/s - (X-cx)b/(a s) - 2nb/s + n
    left_c = (1 / a, 0, -cx / a + n,
              -b / (a * s), 1 / s, cx * b / (a * s) - cy / s - 2 * n * b / s + n)
    # Right face (u = n, v = n - tu): X = cx + tu*a, Y = cy + (2n - tu)b - (n - tv)s
    # tu = (X-cx)/a ; tv = (Y - cy - (2n - tu)b)/s + n = (Y-cy)/s + (X-cx)b/(a s) - 2nb/s + n
    right_c = (1 / a, 0, -cx / a,
               b / (a * s), 1 / s, -cx * b / (a * s) - cy / s - 2 * n * b / s + n)

    for tex, over, coeffs, f in ((top, overlay_top, top_c, 1.0), (side, overlay_side, left_c, 0.8), (side, overlay_side, right_c, 0.6)):
        layer = face(tex, n, coeffs, (w, h))
        if over is not None:
            layer = Image.alpha_composite(layer, face(over, n, coeffs, (w, h)))
        out = Image.alpha_composite(out, shade(layer, f))
    return out.crop(out.getbbox())


CUBE_TEXTURES = {"all": ("all", "all"), "end": ("end", "side"), "top": ("top", "side")}


def render_block_cube(name):
    """Renders a full-cube block model, following one level of parent, or returns None."""
    m = model("block", name)
    t = m.get("textures", {})
    for key, (top_key, side_key) in CUBE_TEXTURES.items():
        if key in t and side_key in t:
            top = texture(t[top_key])
            side = texture(t[side_key])
            o_top = texture(t["overlay_end"]) if "overlay_end" in t else None
            o_side = texture(t["overlay"]) if "overlay" in t else None
            return cube(top, side, o_top, o_side)
    return None


def render_item(name):
    m = model("item", name)
    parent = m.get("parent", "")
    if parent in ("minecraft:item/generated", "item/generated", "minecraft:item/handheld"):
        layers = [texture(m["textures"][k]) for k in sorted(m["textures"]) if k.startswith("layer")]
        img = layers[0]
        for extra in layers[1:]:
            img = Image.alpha_composite(img, extra)
        return sprite(img)
    if parent.startswith("fairygarden:block/"):
        block = parent.split("/", 1)[1]
        rendered = render_block_cube(block)
        if rendered is not None:
            return rendered
    return None


# Block states the cards show, drawn from their block textures.
STATES = {
    **{f"starbloom@{i}": f"fairygarden:block/starbloom_stage{i}" for i in range(4)},
    **{f"strawberry_bush@{i}": f"fairygarden:block/strawberry_bush_stage{i}" for i in range(4)},
    "moonflower@open": "fairygarden:block/moonflower_open",
    "moonflower@closed": "fairygarden:block/moonflower",
    "fairy_crystal_cluster@block": "fairygarden:block/fairy_crystal_cluster",
    "glimmer_lily@block": "fairygarden:block/glimmer_lily",
    "moonbell@block": "fairygarden:block/moonbell",
    "tall_lavender@top": "fairygarden:block/tall_lavender_top",
}
# Butterflies, from their own entity skins: the four wings, below the body strip at the top of the skin.
CREATURES = {f"butterfly@{c}": f"fairygarden:entity/butterfly/{c}" for c in ("pink", "lavender", "cream", "mint", "sky", "white")}


def main():
    OUT.mkdir(exist_ok=True)
    names = sorted(p.stem for p in (ASSETS_GEN / "models" / "item").glob("*.json"))
    done, skipped = 0, []
    for name in names:
        img = render_item(name)
        if img is None:
            skipped.append(name)
            continue
        img.save(OUT / f"{name}.png")
        done += 1
    for name, ref in STATES.items():
        sprite(texture(ref)).save(OUT / f"{name}.png")
        done += 1
    for name, ref in CREATURES.items():
        skin = texture(ref)
        wings = skin.crop((0, 7, skin.width, skin.height))
        sprite(wings.crop(wings.getbbox())).save(OUT / f"{name}.png")
        done += 1
    print(f"rendered {done} images into {OUT}")
    if skipped:
        print("not a flat item or a full cube, left out:", " ".join(skipped))


if __name__ == "__main__":
    main()
