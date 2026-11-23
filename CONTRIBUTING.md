# Contributing

Thanks for helping improve Fairy Garden.

## How to contribute

1. Fork the repository and create a branch from `main`.
2. Make focused changes in the relevant Minecraft version workspace (`1.20.1/`, `1.21.1/`, `26.2/` or `26.3/`).
   A gameplay change usually belongs in all four: the `common/` code is nearly identical between them, and only the
   version-specific APIs differ.
3. Build the workspace you changed (`./gradlew build` from that folder: Gradle on Java 21 for 1.20.1 and 1.21.1,
   Java 25 for 26.x), or all four with `./gradlew buildAll` from the root (the 8 loader jars land in `releases/`).
4. Open a pull request describing the change and how you tested it.

## Layout

Each workspace is a MultiLoader Gradle build:

- `common/`: blocks, items, creatures, world generation (biomes through TerraBlender, features, structures), config,
  ambience and the client renderers. Nearly all changes happen here.
- `fabric/` and `neoforge/` (`forge/` on 1.20.1): entry points that register content, the creative tab, renderers,
  particles and events through the `platform/` services. The 1.20.1 Forge module also holds the data generators,
  the GameTests, the world generation self test and the snapshot/showcase tours (development only, left out of the
  jar).

## Assets and data

The 1.20.1 workspace holds the canonical resources. Never edit the converted copies in the other workspaces by hand:

```bash
python3 tools/generate_textures.py         # every texture, into 1.20.1/common
cd 1.20.1 && ./gradlew :forge:runData      # models, blockstates, lang, loot, recipes, tags, worldgen, structures
python3 tools/port_data.py                 # converts them for 1.21.1, 26.2 and 26.3
python3 tools/port_data.py --check         # what CI runs: the committed copies match a fresh conversion
```

## Testing in game

```bash
cd 1.20.1 && ./gradlew :forge:runGameTestServer   # the automated in-game tests
cd 1.20.1 && ./gradlew :forge:runWorldgenTest     # finds, generates and counts every biome and structure
cd 1.20.1 && ./gradlew :forge:runClient -Pshowcase   # a guided tour of every biome and structure
scripts/server-smoke-test.sh 26.3 fabric          # a dev server that places every block and locates every biome
```

If you change gameplay, try it on every workspace you touched, for Fabric and NeoForge/Forge.

## Coding expectations

- Prefer small, reviewable PRs over large mixed changes.
- Match existing naming, package layout, and MultiLoader patterns.
- Keep world generation on the server; particles, sounds and rendering stay client-side.
- Do not commit build output, IDE metadata, or local secrets.
- When fixing a bug, say which version/loaders you verified.

## Issues

Use GitHub Issues for bugs and feature requests. Include Minecraft version, mod loader, mod version, TerraBlender
version, and steps to reproduce when reporting bugs.

## Licensing

By contributing, you agree that your contributions are licensed under the Apache License, Version 2.0, unless you
state otherwise in the pull request.
