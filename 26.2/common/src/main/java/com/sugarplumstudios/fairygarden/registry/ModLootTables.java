package com.sugarplumstudios.fairygarden.registry;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import com.sugarplumstudios.fairygarden.FairyGarden;

public final class ModLootTables {
    public static final ResourceKey<LootTable> FLOWER_SHRINE = key("chests/flower_shrine");
    public static final ResourceKey<LootTable> HIDDEN_FAIRY_GARDEN = key("chests/hidden_fairy_garden");
    public static final ResourceKey<LootTable> MOONSTONE_CIRCLE = key("chests/moonstone_circle");
    public static final ResourceKey<LootTable> MUSHROOM_COTTAGE = key("chests/mushroom_cottage");
    public static final ResourceKey<LootTable> FAIRY_CACHE = key("chests/fairy_cache");

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, FairyGarden.id(path));
    }

    private ModLootTables() {
    }
}
