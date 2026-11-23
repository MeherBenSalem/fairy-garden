package com.sugarplumstudios.fairygarden.forge;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.client.AmbientEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client game events: the biome ambience runs at the end of every client tick. */
@Mod.EventBusSubscriber(modid = FairyGarden.MOD_ID, value = Dist.CLIENT)
public final class ForgeClientEvents {
    private ForgeClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            AmbientEffects.tick();
        }
    }
}
