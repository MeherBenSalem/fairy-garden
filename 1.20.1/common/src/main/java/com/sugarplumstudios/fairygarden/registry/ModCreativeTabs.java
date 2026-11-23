package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.platform.Services;
import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

public final class ModCreativeTabs {

    public static final RegistryEntry<CreativeModeTab> FAIRY_GARDEN = Services.PLATFORM.register(Registries.CREATIVE_MODE_TAB, "fairy_garden", () -> Services.PLATFORM.creativeTab()
            .title(Component.translatable("itemGroup.fairygarden"))
            .icon(() -> new ItemStack(ModBlocks.CRYSTAL_FLOWER.get()))
            .displayItems((params, output) -> {
                ModItems.BLOCK_TAB_ORDER.forEach(item -> output.accept(item.get()));
                ModItems.ITEM_TAB_ORDER.forEach(item -> output.accept(item.get()));
            })
            .build());

    /** Loads the class, which registers everything above. */
    public static void init() {
    }

    private ModCreativeTabs() {
    }
}
