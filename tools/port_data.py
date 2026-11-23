#!/usr/bin/env python3
"""Derives the 1.21.1, 26.2 and 26.3 resources of Fairy Garden from the 1.20.1 ones.

The 1.20.1 workspace is the source: 1.20.1/common/src/main/resources (textures, sounds, hand-made models, the GameTest
structure) and 1.20.1/common/src/generated/resources (what the Forge data generators write: blockstates, models, lang,
loot tables, recipes, advancements, tags and the whole worldgen data pack). This script copies both trees into each
newer workspace's common/src/{main,generated}/resources and rewrites the data in that version's formats:

    python3 tools/port_data.py            # rewrite every target
    python3 tools/port_data.py 26.3       # one target
    python3 tools/port_data.py --check    # fail if a committed target differs from a fresh conversion (CI)

Standard library only. Each target's pack.mcmeta (and, on 26.x, META-INF/accesstransformer.cfg) is hand-written in its
common/src/main/resources and kept.

1.21.1
- data folders renamed (recipe/, loot_table/, advancement/, structure/, tags/item, tags/block, ...);
- forge: convention tags become c: tags (both NeoForge and Fabric read c: on 1.21);
- recipe results are {"id", "count"}; advancement icons are {"id"};
- item predicates: a tag is "items": "#tag"; location predicates use "biomes"/"structures";
- loot: match_tool's enchantment list is a minecraft:enchantments item sub-predicate; looting_enchant became
  enchanted_count_increase; minecraft:grass is minecraft:short_grass;
- worldgen int providers lost their "value" wrapper;
- block models: Forge's per-element "forge_data" (full-bright faces) is NeoForge's "neoforge_data";
- NeoForge reads composting chances from the neoforge:compostables data map (written here, also for 26.2).
26.2 (and 26.3)
- recipe ingredients are an item id, a "#tag" or a list of those;
- advancement backgrounds name a texture without "textures/" and ".png";
- minecraft:random_patch and minecraft:flower are gone: a patch is its inner feature, placed with count +
  random_offset (trapezoid spreads) + the inner placement, appended to the placed features that used it;
- tree configs lost dirt_provider/force_dirt; what goes under the trunk is below_trunk_provider;
- biomes: sky/fog/water fog colours, ambient sounds, particles and music are environment attributes, the colours
  left in "effects" are "#rrggbb" strings, and "carvers" is one list;
- every item needs an item model definition (assets/<ns>/items/<id>.json); spawn eggs use a two-layer
  egg texture tinted with their colours (the vanilla template_spawn_egg model is gone);
- block models: "render_type" is gone (the texture's alpha decides) and full-bright faces use the element's
  vanilla "light_emission";
- the flower crown is drawn from assets/<ns>/equipment/flower_crown.json (texture under
  textures/entity/equipment/humanoid/); each wood has its own boat and chest boat entity types (lang names added).
26.3 only
- loot conditions and functions are keyed by "type"; a "condition" field holds one condition (several are wrapped
  in minecraft:all_of), "functions" became "modifier", rolls and counts are integer providers, and
  block_state_property became match_block;
- recipe_unlocked takes a "recipes" list;
- configured features live in worldgen/feature with their config flattened into the feature object; block states
  are {"id", "properties"}; simple state providers are the bare state; weighted_state_provider is minecraft:weighted;
  random_offset became minecraft:offset (x/y/z);
- biome mob spawns are the minecraft:gameplay/natural_mob_spawns attribute; spawn entries carry one "count";
- block model elements: "shade": false became "shade_direction_override": "up".
"""
from __future__ import annotations

import filecmp
import json
import re
import shutil
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "1.20.1" / "common" / "src"
ASSETS_EXTRA = Path(__file__).resolve().parent / "port_data_assets"
M = "fairygarden"
TARGETS = ("1.21.1", "26.2", "26.3")

RENAMED = {"loot_tables": "loot_table", "recipes": "recipe", "structures": "structure", "advancements": "advancement"}
RENAMED_TAGS = {"items": "item", "blocks": "block", "entity_types": "entity_type", "fluids": "fluid",
                "game_events": "game_event"}
# forge:<path> convention tags -> c:<path> (1.21 common tags). Unlisted paths keep their name.
C_TAGS = {
    "item": {"fruits": "foods/fruit", "fruits/strawberry": "foods/fruit/strawberry", "fences/wooden": "fences/wooden",
             "fence_gates/wooden": "fence_gates/wooden", "gems": "gems", "seeds": "seeds", "crops": "crops"},
    "block": {"fences/wooden": "fences/wooden", "fence_gates/wooden": "fence_gates/wooden"},
    "worldgen/biome": {"is_dense/overworld": "is_dense_vegetation/overworld",
                       "is_sparse/overworld": "is_sparse_vegetation/overworld"},
}
RENAMED_IDS_121 = {"minecraft:grass": "minecraft:short_grass"}
RENAMED_IDS_26 = {"minecraft:chain": "minecraft:iron_chain"}
COOKING = ("minecraft:smelting", "minecraft:smoking", "minecraft:campfire_cooking", "minecraft:blasting")
INT_PROVIDERS = ("minecraft:uniform", "minecraft:biased_to_bottom", "minecraft:clamped", "minecraft:clamped_normal")
SPAWN_EGGS = Path("com/sugarplumstudios/fairygarden/registry/ModItems.java")


def load(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def dump(path: Path, obj) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def walk(node, fn):
    """Applies fn bottom-up to every dict and list."""
    if isinstance(node, dict):
        return fn({k: walk(v, fn) for k, v in node.items()})
    if isinstance(node, list):
        return fn([walk(v, fn) for v in node])
    return node


def rename_ids(node, table):
    if isinstance(node, dict):
        return {k: rename_ids(v, table) for k, v in node.items()}
    if isinstance(node, list):
        return [rename_ids(v, table) for v in node]
    if isinstance(node, str):
        prefix = "#" if node.startswith("#") else ""
        return prefix + table.get(node[len(prefix):], node[len(prefix):])
    return node


def hex_colour(value):
    return f"#{value & 0xFFFFFF:06x}" if isinstance(value, int) else value


# =========================================================================== 1.21.1

def recipe_121(body: dict) -> dict:
    result = body.get("result")
    if body.get("type") in COOKING and isinstance(result, str):
        body["result"] = {"id": result}
    elif isinstance(result, dict) and "item" in result:
        result["id"] = result.pop("item")
        result.pop("nbt", None)
    return body


def item_predicate_121(pred: dict) -> dict:
    pred = dict(pred)
    if "tag" in pred:
        pred["items"] = "#" + pred.pop("tag")
    pred.pop("nbt", None)
    enchantments = pred.pop("enchantments", None)
    if enchantments:
        pred.setdefault("predicates", {})["minecraft:enchantments"] = [
            {"enchantments": e["enchantment"], **({"levels": e["levels"]} if "levels" in e else {})}
            for e in enchantments]
    return pred


def location_121(loc: dict) -> dict:
    loc = dict(loc)
    if "biome" in loc:
        loc["biomes"] = loc.pop("biome")
    if "structure" in loc:
        loc["structures"] = loc.pop("structure")
    return loc


def advancement_121(body: dict) -> dict:
    icon = body.get("display", {}).get("icon")
    if isinstance(icon, dict) and "item" in icon:
        body["display"]["icon"] = {"id": icon["item"]}
    for criterion in body.get("criteria", {}).values():
        conditions = criterion.get("conditions", {})
        if criterion.get("trigger") == "minecraft:inventory_changed" and "items" in conditions:
            conditions["items"] = [item_predicate_121(p) for p in conditions["items"]]

        def fix(node):
            if isinstance(node, dict) and isinstance(node.get("location"), dict):
                node = {**node, "location": location_121(node["location"])}
            return node
        criterion["conditions"] = walk(conditions, fix)
    return body


def loot_121(body):
    def fix(node):
        if not isinstance(node, dict):
            return node
        if node.get("condition") == "minecraft:match_tool" and isinstance(node.get("predicate"), dict):
            node = {**node, "predicate": item_predicate_121(node["predicate"])}
        if node.get("function") == "minecraft:looting_enchant":
            node = {k: v for k, v in node.items() if k != "function"}
            node = {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting", **node}
        return node
    return rename_ids(walk(body, fix), RENAMED_IDS_121)


def int_providers_121(body):
    """{"type": "minecraft:uniform", "value": {"min_inclusive", "max_inclusive"}} -> flat (1.20.5+)."""
    def fix(node):
        if (isinstance(node, dict) and node.get("type") in INT_PROVIDERS and set(node) == {"type", "value"}
                and isinstance(node["value"], dict)):
            return {"type": node["type"], **node["value"]}
        return node
    return walk(body, fix)


def model_121(body: dict) -> dict:
    for element in body.get("elements", []) if isinstance(body.get("elements"), list) else []:
        if isinstance(element, dict) and "forge_data" in element:
            element["neoforge_data"] = element.pop("forge_data")
    return body


def forge_tags_to_c(data: Path) -> None:
    forge = data / "forge" / "tags"
    if not forge.exists():
        return
    for path in sorted(forge.rglob("*.json")):
        rel = path.relative_to(forge).as_posix()[:-5]
        kind = next((k for k in C_TAGS if rel.startswith(k + "/")), None)
        if kind is None:
            continue
        tag = rel[len(kind) + 1:]
        target = data / "c" / "tags" / kind / (C_TAGS[kind].get(tag, tag) + ".json")
        body = load(path)
        values = []
        for v in body["values"]:
            ref = v["id"] if isinstance(v, dict) else v
            if ref.startswith("#forge:"):
                inner = ref[len("#forge:"):]
                v = "#c:" + C_TAGS[kind].get(inner, inner)
            values.append(v)
        if target.exists():
            values = load(target)["values"] + [v for v in values if v not in load(target)["values"]]
        dump(target, {**body, "values": values})
    shutil.rmtree(data / "forge")


def port_121(tree: Path) -> None:
    data = tree / "data"
    if data.exists():
        for ns_dir in data.iterdir():
            for old, new in RENAMED.items():
                if (ns_dir / old).exists():
                    (ns_dir / old).rename(ns_dir / new)
            tags = ns_dir / "tags"
            if tags.exists():
                for old, new in RENAMED_TAGS.items():
                    if (tags / old).exists():
                        (tags / old).rename(tags / new)
        forge_tags_to_c(data)
        for path in sorted(data.rglob("*.json")):
            rel = path.relative_to(data).as_posix()
            folder = rel.split("/")[1] if rel.count("/") >= 2 else ""
            body = load(path)
            if folder == "recipe":
                body = recipe_121(body)
            elif folder == "advancement":
                body = advancement_121(body)
            elif folder == "loot_table":
                body = loot_121(body)
            elif folder == "worldgen":
                body = int_providers_121(body)
            elif folder == "tags":
                body = rename_ids(body, RENAMED_IDS_121)
            dump(path, body)
    models = tree / "assets" / M / "models"
    if models.exists():
        for path in sorted(models.rglob("*.json")):
            body = load(path)
            if "forge_data" in path.read_text(encoding="utf-8"):
                dump(path, model_121(body))


# Composting chances, as CommonSetup.registerCompostables puts them into the composter's table (1.20.1 Forge, Fabric).
# NeoForge 1.21.1 and 26.2 read them from the neoforge:compostables data map instead; 26.3 keeps them on the items.
COMPOSTABLES = {
    **{name: 0.3 for name in (
        "pink_flower_carpet", "lavender_flower_carpet", "cream_flower_carpet", "mint_flower_carpet", "blue_flower_carpet",
        "blossom_leaves", "snow_blossom_leaves", "moonwood_leaves", "blossom_sapling", "snow_blossom_sapling",
        "moonwood_sapling", "blush_grass", "moonlit_grass", "blossom_petals", "snow_blossom_petals", "strawberry",
        "starbloom_seeds", "moon_petal")},
    **{name: 0.65 for name in (
        "rosy_daisy", "lavender", "cream_buttercup", "mint_posy", "starflower", "bluebell", "moonflower", "crystal_flower",
        "tall_lavender", "fairy_foxglove", "fairy_mushroom", "glowcap", "pink_hydrangea", "blue_hydrangea",
        "lavender_hydrangea", "glimmer_lily", "moonbell")},
    **{name: 0.85 for name in ("fairy_mushroom_block", "glowcap_block", "lavender_bale", "strawberry_shortcake")},
}


def compostables_data_map(generated: Path) -> None:
    dump(generated / "data" / "neoforge" / "data_maps" / "item" / "compostables.json",
         {"values": {f"{M}:{name}": {"chance": chance} for name, chance in sorted(COMPOSTABLES.items())}})


# =========================================================================== 26.2

def ingredient_26(value):
    if isinstance(value, list):
        return [ingredient_26(v) for v in value]
    if isinstance(value, dict):
        if "tag" in value:
            return "#" + value["tag"]
        if "item" in value:
            return value["item"]
    return value


def recipe_26(body: dict) -> dict:
    if "key" in body:
        body["key"] = {k: ingredient_26(v) for k, v in body["key"].items()}
    for field in ("ingredient", "base", "addition", "template"):
        if field in body:
            body[field] = ingredient_26(body[field])
    if "ingredients" in body:
        body["ingredients"] = [ingredient_26(i) for i in body["ingredients"]]
    return body


def advancement_26(body: dict) -> dict:
    display = body.get("display", {})
    background = display.get("background")
    if background:
        ns, path = background.split(":", 1) if ":" in background else ("minecraft", background)
        if path.startswith("textures/") and path.endswith(".png"):
            display["background"] = f"{ns}:{path[len('textures/'):-len('.png')]}"
    return body


def trapezoid(spread: int) -> dict:
    return {"type": "minecraft:trapezoid", "min": -spread, "max": spread, "plateau": 0}


PATCH_TYPES = ("minecraft:random_patch", "minecraft:flower", "minecraft:no_bonemeal_flower")


def split_patches(data: Path) -> None:
    """random_patch/flower configured features -> their inner feature; the patch spread moves onto the placed
    features that used them."""
    configured = data / M / "worldgen" / "configured_feature"
    placed = data / M / "worldgen" / "placed_feature"
    users = {}
    for path in sorted(placed.rglob("*.json")):
        users.setdefault(load(path)["feature"], []).append(path)
    for path in sorted(configured.rglob("*.json")):
        body = load(path)
        if body.get("type") not in PATCH_TYPES:
            continue
        config = body["config"]
        inner = config["feature"]
        if isinstance(inner, str) or "feature" not in inner:
            raise SystemExit(f"{path.name}: a patch whose inner feature is not inline")
        dump(path, inner["feature"])
        key = f"{M}:{path.relative_to(configured).as_posix()[:-5]}"
        if key not in users:
            raise SystemExit(f"{path.name}: a patch that no placed feature uses")
        for placed_path in users[key]:
            placed_body = load(placed_path)
            placed_body["placement"] += [
                {"type": "minecraft:count", "count": config.get("tries", 128)},
                {"type": "minecraft:random_offset", "xz_spread": trapezoid(config.get("xz_spread", 7)),
                 "y_spread": trapezoid(config.get("y_spread", 3))},
            ] + inner.get("placement", [])
            dump(placed_path, placed_body)


BELOW_TRUNK_262 = {
    "type": "minecraft:rule_based_state_provider",
    "rules": [{
        "if_true": {"type": "minecraft:not", "predicate": {"type": "minecraft:matching_block_tag",
                                                           "tag": "minecraft:cannot_replace_below_tree_trunk"}},
        "then": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:dirt"}},
    }],
}
BELOW_TRUNK_263 = "minecraft:soil_beneath_tree"


def trees(data: Path, v263: bool) -> None:
    configured = data / M / "worldgen" / "configured_feature"
    for path in sorted(configured.rglob("*.json")):
        body = load(path)
        if body.get("type") != "minecraft:tree":
            continue
        config = body["config"]
        config.pop("dirt_provider", None)
        config.pop("force_dirt", None)
        config["below_trunk_provider"] = BELOW_TRUNK_263 if v263 else BELOW_TRUNK_262
        dump(path, body)


def biome_26(body: dict) -> dict:
    """1.21.1 biome effects -> 26.x environment attributes (the layout vanilla's own biomes use)."""
    effects = body.pop("effects", {})
    attributes = {}
    for key in ("sky_color", "fog_color", "water_fog_color"):
        if key in effects:
            attributes[f"minecraft:visual/{key}"] = hex_colour(effects.pop(key))
    sounds = {}
    if "ambient_sound" in effects:
        sounds["loop"] = effects.pop("ambient_sound")
    if "mood_sound" in effects:
        sounds["mood"] = effects.pop("mood_sound")
    if "additions_sound" in effects:
        sounds["additions"] = effects.pop("additions_sound")
    if sounds:
        attributes["minecraft:audio/ambient_sounds"] = sounds
    if "particle" in effects:
        particle = effects.pop("particle")
        attributes["minecraft:visual/ambient_particles"] = [
            {"particle": particle["options"], "probability": particle["probability"]}]
    if "music" in effects:
        music = effects.pop("music")
        music.pop("replace_current_music", None)
        attributes["minecraft:audio/background_music"] = {"default": music}
    out = {"attributes": attributes}
    out.update(body)
    carvers = out.get("carvers")
    if isinstance(carvers, dict):
        out["carvers"] = [c for step in carvers.values() for c in (step if isinstance(step, list) else [step])]
    out["effects"] = {k: hex_colour(v) if k.endswith("_color") else v for k, v in effects.items()}
    return out


def tinted(assets: Path, model: Path) -> bool:
    seen = set()
    path = model
    while path is not None and path not in seen and path.exists():
        seen.add(path)
        text = path.read_text(encoding="utf-8")
        if '"tintindex"' in text:
            return True
        parent = json.loads(text).get("parent", "")
        if not parent.startswith(M + ":"):
            return False
        path = assets / "models" / (parent.split(":", 1)[1] + ".json")
    return False


def spawn_egg_colours() -> dict:
    """butterfly_spawn_egg -> (background, highlight), read from the 1.20.1 ModItems."""
    text = (SOURCE / "main" / "java" / SPAWN_EGGS).read_text(encoding="utf-8")
    found = re.findall(r'register\("(\w+_spawn_egg)",\s*\(\)\s*->\s*Services\.PLATFORM\.spawnEgg\(\s*ModEntities\.\w+,\s*'
                       r'0x([0-9A-Fa-f]{6}),\s*0x([0-9A-Fa-f]{6})', text)
    return {name: (int(a, 16), int(b, 16)) for name, a, b in found}


def item_definitions(main: Path, generated: Path) -> None:
    """Every item model gets assets/<ns>/items/<id>.json (written into the generated tree)."""
    eggs = spawn_egg_colours()
    out = generated / "assets" / M / "items"
    for root in (main, generated):
        assets = root / "assets" / M
        for model in sorted((assets / "models" / "item").glob("*.json")):
            name = model.stem
            if name in eggs:
                background, highlight = eggs[name]
                definition = {"type": "minecraft:model", "model": f"{M}:item/{name}",
                              "tints": [{"type": "minecraft:constant", "value": background},
                                        {"type": "minecraft:constant", "value": highlight}]}
                dump(model, {"parent": "minecraft:item/generated",
                             "textures": {"layer0": f"{M}:item/spawn_egg", "layer1": f"{M}:item/spawn_egg_overlay"}})
            else:
                definition = {"type": "minecraft:model", "model": f"{M}:item/{name}"}
                if tinted(assets, model):
                    definition["tints"] = [{"type": "minecraft:constant", "value": -1}]
            dump(out / f"{name}.json", {"model": definition})


def model_26(body: dict, v263: bool) -> dict:
    body.pop("render_type", None)
    for element in body.get("elements", []) if isinstance(body.get("elements"), list) else []:
        if not isinstance(element, dict):
            continue
        light = element.pop("neoforge_data", None) or element.pop("forge_data", None)
        if light:
            element["light_emission"] = max(light.get("block_light", 0), light.get("sky_light", 0))
        if v263 and isinstance(element.get("shade"), bool):
            if not element.pop("shade"):
                element["shade_direction_override"] = "up"
    return body


def port_26(main: Path, generated: Path, v263: bool) -> None:
    for tree in (main, generated):
        data = tree / "data"
        if (data / M / "worldgen" / "configured_feature").exists():
            split_patches(data)
            trees(data, v263)
        if data.exists():
            for path in sorted(data.rglob("*.json")):
                rel = path.relative_to(data).as_posix()
                folder = rel.split("/")[1] if rel.count("/") >= 2 else ""
                body = rename_ids(load(path), RENAMED_IDS_26)
                if folder == "recipe":
                    body = recipe_26(body)
                elif folder == "advancement":
                    body = advancement_26(body)
                elif rel.startswith(f"{M}/worldgen/biome/"):
                    body = biome_26(body)
                dump(path, body)
        models = tree / "assets" / M / "models"
        if models.exists():
            for path in sorted(models.rglob("*.json")):
                body = load(path)
                changed = model_26(json.loads(json.dumps(body)), v263)
                if changed != body:
                    dump(path, changed)
    textures = main / "assets" / M / "textures" / "item"
    for name in ("spawn_egg.png", "spawn_egg_overlay.png"):
        shutil.copyfile(ASSETS_EXTRA / name, textures / name)
    item_definitions(main, generated)
    equipment_and_boats_26(main, generated)


WOODS = ("blossom", "moonwood")


def equipment_and_boats_26(main: Path, generated: Path) -> None:
    """The flower crown is drawn from an equipment asset; each wood has its own boat entity types."""
    assets = main / "assets" / M
    armor = assets / "textures" / "models" / "armor"
    humanoid = assets / "textures" / "entity" / "equipment" / "humanoid"
    humanoid.mkdir(parents=True, exist_ok=True)
    shutil.move(str(armor / "flower_crown_layer_1.png"), humanoid / "flower_crown.png")
    shutil.rmtree(assets / "textures" / "models")
    dump(generated / "assets" / M / "equipment" / "flower_crown.json",
         {"layers": {"humanoid": [{"texture": f"{M}:flower_crown"}]}})
    lang_path = generated / "assets" / M / "lang" / "en_us.json"
    lang = load(lang_path)
    lang.pop(f"entity.{M}.boat", None)
    lang.pop(f"entity.{M}.chest_boat", None)
    for wood in WOODS:
        for kind in ("boat", "chest_boat"):
            lang[f"entity.{M}.{wood}_{kind}"] = lang[f"item.{M}.{wood}_{kind}"]
    dump(lang_path, dict(sorted(lang.items())))


# =========================================================================== 26.3

def is_condition_list(value) -> bool:
    return isinstance(value, list) and bool(value) and all(
        isinstance(v, dict) and isinstance(v.get("condition"), str) for v in value)


def condition_263(cond: dict) -> dict:
    cond = dict(cond)
    if cond["condition"] == "minecraft:block_state_property":
        cond = {"condition": "minecraft:match_block", "blocks": cond["block"],
                **({"state": cond["properties"]} if "properties" in cond else {})}
    out = {"type": cond.pop("condition")}
    for key, value in cond.items():
        if key == "terms":
            out[key] = [condition_263(t) for t in value]
        elif key == "term":
            out[key] = condition_263(value)
        else:
            out[key] = value
    return out


def conditions_263(conds: list) -> dict:
    terms = [condition_263(c) for c in conds]
    return terms[0] if len(terms) == 1 else {"type": "minecraft:all_of", "terms": terms}


def int_provider(value):
    if isinstance(value, float) and value.is_integer():
        return int(value)
    if isinstance(value, dict):
        return {k: int_provider(v) if k in ("min", "max", "value", "n") else v for k, v in value.items()}
    return value


def function_263(fn: dict) -> dict:
    out = {"type": fn["function"]}
    for key, value in fn.items():
        if key == "function":
            continue
        if key == "conditions":
            out["condition"] = conditions_263(value)
        elif fn["function"] == "minecraft:set_count" and key == "count":
            out[key] = int_provider(value)
        else:
            out[key] = value
    return out


def loot_263(node):
    if isinstance(node, list):
        return [loot_263(v) for v in node]
    if not isinstance(node, dict):
        return node
    out = {}
    for key, value in node.items():
        if key == "conditions" and is_condition_list(value):
            out["condition"] = conditions_263(value)
        elif key == "functions":
            out["modifier"] = [function_263(f) for f in value]
        elif key == "rolls":
            out[key] = int_provider(value)
        else:
            out[key] = loot_263(value)
    return out


def advancement_263(body: dict) -> dict:
    for criterion in body.get("criteria", {}).values():
        conditions = criterion.get("conditions", {})
        if criterion.get("trigger") == "minecraft:recipe_unlocked" and isinstance(conditions.get("recipe"), str):
            conditions["recipes"] = [conditions.pop("recipe")]
        for key, value in list(conditions.items()):
            if is_condition_list(value):
                conditions[key] = conditions_263(value)
    return body


def block_states_263(node):
    if isinstance(node, list):
        return [block_states_263(v) for v in node]
    if not isinstance(node, dict):
        return node
    if isinstance(node.get("Name"), str) and set(node) <= {"Name", "Properties"}:
        state = {"id": node["Name"]}
        if "Properties" in node:
            state["properties"] = node["Properties"]
        return state
    if node.get("type") == "minecraft:simple_state_provider" and "state" in node:
        return block_states_263(node["state"])
    if node.get("type") == "minecraft:weighted_state_provider":
        return {"type": "minecraft:weighted", **{k: block_states_263(v) for k, v in node.items() if k != "type"}}
    if node.get("type") == "minecraft:random_offset":
        return {"type": "minecraft:offset", "x": node.get("xz_spread", 0), "y": node.get("y_spread", 0),
                "z": node.get("xz_spread", 0)}
    return {k: block_states_263(v) for k, v in node.items()}


def spawn_entry_263(entry: dict) -> dict:
    lo, hi = entry.get("minCount", 1), entry.get("maxCount", 1)
    count = lo if lo == hi else {"type": "minecraft:uniform", "min_inclusive": lo, "max_inclusive": hi}
    return {"type": entry["type"], "count": count, "weight": entry["weight"]}


def biome_263(body: dict) -> dict:
    spawners = body.pop("spawners", {})
    costs = body.pop("spawn_costs", {})
    by_category = {cat: [spawn_entry_263(e) for e in entries] for cat, entries in spawners.items() if entries}
    if by_category or costs:
        argument = {}
        if costs:
            argument["spawn_costs"] = costs
        argument["spawns_by_category"] = by_category
        body["attributes"]["minecraft:gameplay/natural_mob_spawns"] = {"argument": argument, "modifier": "overlay"}
    return body


def structure_263(body: dict) -> dict:
    def fix(node):
        if isinstance(node, dict) and "minCount" in node and "maxCount" in node and "type" in node:
            return spawn_entry_263(node)
        return node
    return walk(body, fix)


def port_263(main: Path, generated: Path) -> None:
    for tree in (main, generated):
        data = tree / "data"
        if not data.exists():
            continue
        for path in sorted(data.rglob("*.json")):
            rel = path.relative_to(data).as_posix()
            folder = rel.split("/")[1] if rel.count("/") >= 2 else ""
            body = load(path)
            if folder == "loot_table":
                body = loot_263(body)
            elif folder == "advancement":
                body = advancement_263(body)
            elif rel.startswith(f"{M}/worldgen/biome/"):
                body = biome_263(body)
            elif rel.startswith(f"{M}/worldgen/structure/"):
                body = structure_263(body)
            dump(path, body)
        worldgen = data / M / "worldgen"
        configured = worldgen / "configured_feature"
        if configured.exists():
            for path in sorted(configured.rglob("*.json")):
                body = load(path)
                flat = {"type": body["type"], **body.get("config", {})}
                dump(worldgen / "feature" / path.relative_to(configured), block_states_263(flat))
            shutil.rmtree(configured)
        for folder in ("placed_feature", "processor_list"):
            for path in sorted((worldgen / folder).rglob("*.json")):
                dump(path, block_states_263(load(path)))


# =========================================================================== driver

def convert(version: str, out_root: Path) -> None:
    """Writes <out_root>/{main,generated} for one target."""
    main, generated = out_root / "main", out_root / "generated"
    for name, src, dst in (("main", SOURCE / "main" / "resources", main),
                           ("generated", SOURCE / "generated" / "resources", generated)):
        shutil.copytree(src, dst, ignore=shutil.ignore_patterns(".cache", "pack.mcmeta", "META-INF"))
    port_121(main)
    port_121(generated)
    if version in ("1.21.1", "26.2"):
        compostables_data_map(generated)
    if version.startswith("26."):
        port_26(main, generated, version == "26.3")
    if version == "26.3":
        port_263(main, generated)


def target_dirs(version: str):
    common = ROOT / version / "common" / "src"
    return common / "main" / "resources", common / "generated" / "resources"


# Hand-written files in each target's common/src/main/resources that the conversion keeps as they are.
HAND_WRITTEN = ("pack.mcmeta", "META-INF/accesstransformer.cfg")


def write(version: str) -> None:
    main_dst, generated_dst = target_dirs(version)
    kept = {name: (main_dst / name).read_bytes() for name in HAND_WRITTEN if (main_dst / name).exists()}
    with tempfile.TemporaryDirectory() as tmp:
        convert(version, Path(tmp))
        for sub, dst in (("main", main_dst), ("generated", generated_dst)):
            shutil.rmtree(dst, ignore_errors=True)
            shutil.copytree(Path(tmp) / sub, dst)
    for name, body in kept.items():
        (main_dst / name).parent.mkdir(parents=True, exist_ok=True)
        (main_dst / name).write_bytes(body)
    print(f"ported resources to {version}/common/src/{{main,generated}}/resources")


def diff_trees(a: Path, b: Path, ignore=("pack.mcmeta", "META-INF")) -> list[str]:
    problems = []
    cmp = filecmp.dircmp(a, b, ignore=list(ignore))

    def visit(c, rel):
        for name in c.left_only:
            problems.append(f"missing {rel}{name}")
        for name in c.right_only:
            problems.append(f"stale {rel}{name}")
        for name in c.diff_files:
            problems.append(f"differs {rel}{name}")
        for name in c.funny_files:
            problems.append(f"unreadable {rel}{name}")
        for name, sub in c.subdirs.items():
            visit(sub, f"{rel}{name}/")
    visit(cmp, "")
    return problems


def check(versions) -> int:
    failed = 0
    for version in versions:
        main_dst, generated_dst = target_dirs(version)
        with tempfile.TemporaryDirectory() as tmp:
            convert(version, Path(tmp))
            problems = []
            for sub, dst in (("main", main_dst), ("generated", generated_dst)):
                if not dst.exists():
                    problems.append(f"{dst.relative_to(ROOT)} is missing")
                    continue
                problems += [f"{dst.relative_to(ROOT)}: {p}" for p in diff_trees(Path(tmp) / sub, dst)]
        if not (main_dst / "pack.mcmeta").exists():
            problems.append(f"{version}: no hand-written pack.mcmeta")
        for p in problems[:40]:
            print(f"::error::{version}: {p}")
        if problems:
            print(f"{version}: {len(problems)} differences; run python3 tools/port_data.py {version}")
            failed = 1
        else:
            print(f"{version}: ported resources are up to date")
    return failed


def main(argv) -> int:
    args = [a for a in argv if not a.startswith("--")]
    versions = args or list(TARGETS)
    unknown = [v for v in versions if v not in TARGETS]
    if unknown:
        print(f"usage: {Path(__file__).name} [--check] [{'|'.join(TARGETS)}]", file=sys.stderr)
        return 2
    if "--check" in argv:
        return check(versions)
    for version in versions:
        write(version)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
