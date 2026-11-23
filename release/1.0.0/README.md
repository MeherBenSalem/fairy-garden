# Fairy Garden 1.0.0: release files

One jar, for Minecraft 1.20.1 on Forge. Install it, together with TerraBlender, on both the client and the server. On CurseForge, tick Minecraft 1.20.1, Forge and Java 17, and add TerraBlender as a required dependency.

| File | Minecraft | Loader | Java | Required dependencies |
|---|---|---|---|---|
| `fairygarden-forge-1.20.1-1.0.0.jar` | 1.20.1 | Forge 47 or newer | 17 | [TerraBlender](https://www.curseforge.com/minecraft/mc-mods/terrablender) 3.0.1 or newer (Forge, 1.20.1) |

Built by the Build release jar workflow (`.github/workflows/build-release.yml`), which runs `./gradlew build` and commits the jar here only after CI passed on the same commit:

- **Jar checks:** the file name, `Implementation-Version` and the `mods.toml` version are 1.0.0, `mods.toml` requires TerraBlender, the classes are SRG-mapped for Forge, the generated world-generation data is inside, and the snapshot and showcase tools are left out.
- **GameTests:** `./gradlew runGameTestServer` runs the mod's 13 in-game tests (`gametest/FairyGameTests.java`): fairy dust, starbloom, moonflowers by day and night, strawberry picking, giant petals, wood stripping, lavender tea, lanterns, creatures, registries, loot tables and recipes.
- **World generation:** `./gradlew runWorldgenTest` starts a headless server on seed `fairygarden`, finds every biome and structure, generates them, counts the Fairy Garden blocks and structures placed and must end with `RESULT: PASS`.
- **Server boot:** a real Forge 1.20.1 dedicated server with this jar and TerraBlender in its `mods` folder starts on a normal world, places all 94 Fairy Garden blocks with `/setblock`, locates each of the five biomes and four structures and generates the chunks there, summons a butterfly and a moon wisp, and stops, with no crash and no error naming the mod (`scripts/server-boot-test.sh`).
- **Client launch:** a dev client starts under Xvfb at 1920×1080, flies to every biome and structure, builds the block gallery, screenshots them all and quits (`./gradlew runSnapshots`), with no crash and no error naming the mod.
