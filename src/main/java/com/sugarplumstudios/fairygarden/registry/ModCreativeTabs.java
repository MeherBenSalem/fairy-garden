package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FairyGarden.MOD_ID);

    public static final RegistryObject<CreativeModeTab> FAIRY_GARDEN = TABS.register("fairy_garden", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.fairygarden"))
            .icon(() -> new ItemStack(ModBlocks.CRYSTAL_FLOWER.get()))
            .displayItems((params, output) -> {
                ModItems.BLOCK_TAB_ORDER.forEach(item -> output.accept(item.get()));
                ModItems.ITEM_TAB_ORDER.forEach(item -> output.accept(item.get()));
            })
            .build());

    private ModCreativeTabs() {
    }
}
