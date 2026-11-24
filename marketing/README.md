# Marketing assets: 1.0

Listing copy and artwork for CurseForge (NightBeam publishes to CurseForge only). Nothing here ships in the mod JAR.

## Listing

| File | Use |
| --- | --- |
| [`curseforge/description.md`](curseforge/description.md) | Full CurseForge project description (Markdown) |
| [`curseforge/summary.txt`](curseforge/summary.txt) | Short CurseForge project summary (220 characters) |
| `images/icon.png` | Project avatar |

Image links in the description point at `raw.githubusercontent.com/MeherBenSalem/fairy-garden/main/marketing/images/...`. **They only work for visitors once this folder is on `main` and the repository is public.** While the repository is private, upload the images to CurseForge instead (the description editor takes uploads) and replace each URL.

The description leads with the pitch, then the five biomes, day and night, magical plants, flowers, the two wood sets, exploration, creatures and ambience, crafting, configuration, everything else, downloads and compatibility, the FAQ and a gallery.

### Dependencies

Fairy Garden **requires TerraBlender** on every loader and version, and the Fabric files also require Fabric API. The Publish workflow declares both as required dependencies of each uploaded file on CurseForge. The description says so in the compatibility table, and the banner has a "Requires TerraBlender" pill.

## Adding a Minecraft version

The supported versions are listed in these places:

1. `VERSIONS` at the top of [`source/render.cjs`](source/render.cjs). Add one line, for example `{ minecraft: '26.4', loaders: ['Fabric', 'NeoForge'] },`. The banner's loader pills and version line are built from it. Then rerun the script for `banner`.
2. The table under **Downloads & Compatibility** in `curseforge/description.md`. Add one row per loader, with the matching TerraBlender.
3. The FAQ answer **Which loaders and versions are supported?** in the same file.

No other copy or artwork names a Minecraft version. A new release number goes in `RELEASE` in `render.cjs` and in the banner's alt text on the first line of the description.

## Images

| File | Size | Use |
| --- | --- | --- |
| `images/banner.png` | 1800×600, 738 KB | Description header: Blossom Valley, the title, "A softer, more magical world.", the Forge and TerraBlender pills and the version line |
| `images/icon.png` | 512×512, 151 KB | Project avatar: an open moonflower in a night-sky ring |
| `images/card-biomes.png` | 1600×900, 1296 KB | Blossom Valley, Lavender Fields, Strawberry Meadow and the Fairy Glade; 5 biomes, 4 structures, 90 blocks, 2 creatures |
| `images/card-night.png` | 1600×900, 1158 KB | The Moonlight Forest at night, plus the moonflower closed and open, moonbell, glowcap, moon lantern and moon petal |
| `images/card-plants.png` | 1600×900, 588 KB | The four starbloom stages and fairy dust; the four strawberry bush stages and strawberries |
| `images/card-flowers.png` | 1600×900, 649 KB | The ten fairy flowers, the four giant petals and the flower crown |
| `images/card-woods.png` | 1600×900, 667 KB | Six pieces of each wood set, snow blossom leaves, stripped moonwood and blossom petals |
| `images/card-structures.png` | 1600×900, 1437 KB | The four structures; 4 structures, 5 treasure tables, 13 advancements |
| `images/card-creatures.png` | 1600×900, 1153 KB | The Fairy Glade at night and the six butterfly colourways |
| `images/card-crafting.png` | 1600×900, 663 KB | Eight crafted items with their ingredients |
| `images/card-config.png` | 1600×900, 569 KB | Excerpts of `fairygarden-common.toml` and `fairygarden-client.toml` |
| `images/header-*.png` | 1400×120, transparent, 32–56 KB | 13 section headings for the description |
| `images/divider.png` | 1400×40, transparent | Section divider |
| `images/screenshots/*.jpg` | 1600×900, 168–508 KB | 14 captures: gallery images and card art |

### Where the pictures come from

- **Screenshots** were taken in a Forge dev client at 1920×1080 by the mod's own dev tools, then cropped and scaled to 1600×900. The biome shots, the lavender flower shrine and the mushroom cottage come from the guided showcase tour (`./gradlew runClient -Pshowcase`, `client/dev/ShowcaseTour.java`), cropped above its caption box; the blossom flower shrine, hidden fairy garden, moonstone circle and the two block-gallery shots come from the snapshot run (`./gradlew runSnapshots`, `client/dev/DevSnapshots.java`). Both run in a freshly generated world, so every biome and structure in them was placed by the mod's own world generation. Nothing was edited in.
- **Block and item renders** on the cards and the icon are drawn from the mod's own models and textures without Minecraft by [`source/renders.py`](source/renders.py): isometric cubes for full blocks (with the glowing overlay on moonwood logs), inventory-style sprites for everything else, plus each starbloom and strawberry bush stage, the open and closed moonflower and the butterfly wings from their entity skins. They are in `source/renders/`.

## Regenerating the artwork

The cards, banner, icon and headers are HTML rendered with Playwright from the screenshots, the renders and the bundled fonts:

```bash
python marketing/source/renders.py
```

```bash
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs            # everything
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs banner icon  # selected assets
```

- Set `CHROMIUM=/path/to/chromium` to render with a Chromium other than Playwright's own download.
- `source/backdrop.jpg` is the blurred card background, a copy of `screenshots/fairy-glade.jpg`.
- `source/logo.png` is the mod-list logo (`src/main/resources/fairygarden_logo.png`).
- `source/fonts/` holds Fredoka and Nunito with their SIL Open Font License 1.1 texts. The script uses only these files.
- To retake the screenshots, run the two dev tools above at 1920×1080 (`./gradlew runSnapshots -PshotSize=1920x1080`), crop and scale the shots you want to 1600×900 and save them in `images/screenshots/` under the same names.

## Wording

Every number in the copy and on the cards comes from the 1.0.0 code: `registry/ModBlocks.java` (light levels), `config/FairyConfig.java` (defaults), the recipes, loot tables, tags and advancements in `src/generated/resources`, and `worldgen/ModBiomes.java` (which vanilla biomes each one replaces, features and spawns). 90 blocks means every block id except the four wall signs (the 13 potted plants included); 13 advancements includes the root. Update the copy when those change. Call the five areas **biomes**, the shrine, garden, circle and cottage **structures**, and the buried chest a **fairy cache**.
