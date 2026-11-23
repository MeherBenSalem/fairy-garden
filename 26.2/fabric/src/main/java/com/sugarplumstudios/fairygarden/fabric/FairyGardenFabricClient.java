package com.sugarplumstudios.fairygarden.fabric;

import com.sugarplumstudios.fairygarden.client.AmbientEffects;
import com.sugarplumstudios.fairygarden.client.particle.ButterflyParticle;
import com.sugarplumstudios.fairygarden.client.particle.GlowParticle;
import com.sugarplumstudios.fairygarden.client.particle.PetalParticle;
import com.sugarplumstudios.fairygarden.client.render.ButterflyModel;
import com.sugarplumstudios.fairygarden.client.render.ButterflyRenderer;
import com.sugarplumstudios.fairygarden.client.render.ModModelLayers;
import com.sugarplumstudios.fairygarden.client.render.MoonWispModel;
import com.sugarplumstudios.fairygarden.client.render.MoonWispRenderer;
import com.sugarplumstudios.fairygarden.config.FairyConfig;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.registry.ModParticles;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;

/**
 * Block render layers are inferred from texture transparency in 26.x, and the vanilla sign renderers draw the
 * Fairy Garden signs (their blocks are added to the vanilla sign block entity types), so neither needs a call here.
 */
public final class FairyGardenFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FairyConfig.loadClient();

        EntityRendererRegistry.register(ModEntities.BUTTERFLY.get(), ButterflyRenderer::new);
        EntityRendererRegistry.register(ModEntities.MOON_WISP.get(), MoonWispRenderer::new);
        ModelLayerRegistry.registerModelLayer(ModModelLayers.BUTTERFLY, ButterflyModel::createLayer);
        ModelLayerRegistry.registerModelLayer(ModModelLayers.MOON_WISP, MoonWispModel::createLayer);
        for (WoodSet set : ModBlocks.WOOD_SETS) {
            EntityRendererRegistry.register(set.boatEntity.get(), context -> new BoatRenderer(context, ModModelLayers.boat(set.name, false)));
            EntityRendererRegistry.register(set.chestBoatEntity.get(), context -> new BoatRenderer(context, ModModelLayers.boat(set.name, true)));
            ModelLayerRegistry.registerModelLayer(ModModelLayers.boat(set.name, false), BoatModel::createBoatModel);
            ModelLayerRegistry.registerModelLayer(ModModelLayers.boat(set.name, true), BoatModel::createChestBoatModel);
        }

        ParticleProviderRegistry particles = ParticleProviderRegistry.getInstance();
        particles.register(ModParticles.BLOSSOM_PETAL.get(), PetalParticle.Provider::new);
        particles.register(ModParticles.SNOW_PETAL.get(), PetalParticle.Provider::new);
        particles.register(ModParticles.BUTTERFLY.get(), ButterflyParticle.Provider::new);
        particles.register(ModParticles.FAIRY_SPARKLE.get(), sprites -> new GlowParticle.Provider(sprites, GlowParticle.Style.SPARKLE));
        particles.register(ModParticles.MOON_GLIMMER.get(), sprites -> new GlowParticle.Provider(sprites, GlowParticle.Style.GLIMMER));
        particles.register(ModParticles.POLLEN.get(), sprites -> new GlowParticle.Provider(sprites, GlowParticle.Style.POLLEN));

        ClientTickEvents.END_CLIENT_TICK.register(client -> AmbientEffects.tick());
    }
}
