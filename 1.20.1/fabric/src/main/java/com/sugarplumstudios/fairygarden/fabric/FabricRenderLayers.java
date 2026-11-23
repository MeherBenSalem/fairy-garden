package com.sugarplumstudios.fairygarden.fabric;

import java.util.Set;

/**
 * The blocks whose models ask for a cut-out render type. Forge reads this from the models' "render_type"; Fabric
 * needs it per block. Every other block renders solid. Derived from the block models.
 */
final class FabricRenderLayers {
    static final Set<String> CUTOUT = Set.of(
            "blossom_door", "blossom_petals", "blossom_sapling", "blossom_trapdoor", "blue_flower_carpet",
            "blue_hydrangea", "bluebell", "blush_grass", "cream_buttercup", "cream_flower_carpet", "crystal_flower",
            "fairy_crystal_cluster", "fairy_foxglove", "fairy_lantern", "fairy_mushroom", "glimmer_lily", "glowcap",
            "lavender", "lavender_flower_carpet", "lavender_hydrangea", "mint_flower_carpet", "mint_posy",
            "moon_lantern", "moonbell", "moonflower", "moonlit_grass", "moonwood_door", "moonwood_log",
            "moonwood_sapling", "moonwood_trapdoor", "moonwood_wood", "pink_flower_carpet", "pink_hydrangea",
            "potted_blossom_sapling", "potted_bluebell", "potted_cream_buttercup", "potted_crystal_flower",
            "potted_fairy_mushroom", "potted_glowcap", "potted_lavender", "potted_mint_posy", "potted_moonflower",
            "potted_moonwood_sapling", "potted_rosy_daisy", "potted_snow_blossom_sapling", "potted_starflower",
            "rosy_daisy", "snow_blossom_petals", "snow_blossom_sapling", "starbloom", "starflower", "strawberry_bush",
            "tall_lavender");

    static final Set<String> CUTOUT_MIPPED = Set.of(
            "blossom_leaves", "moonwood_leaves", "snow_blossom_leaves");

    private FabricRenderLayers() {
    }
}
