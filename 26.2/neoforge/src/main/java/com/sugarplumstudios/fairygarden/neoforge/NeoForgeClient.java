package com.sugarplumstudios.fairygarden.neoforge;

import com.sugarplumstudios.fairygarden.client.AmbientEffects;
import com.sugarplumstudios.fairygarden.client.particle.ButterflyParticle;
import com.sugarplumstudios.fairygarden.client.particle.GlowParticle;
import com.sugarplumstudios.fairygarden.client.particle.PetalParticle;
import com.sugarplumstudios.fairygarden.client.render.ButterflyModel;
import com.sugarplumstudios.fairygarden.client.render.ButterflyRenderer;
import com.sugarplumstudios.fairygarden.client.render.ModModelLayers;
import com.sugarplumstudios.fairygarden.client.render.MoonWispModel;
import com.sugarplumstudios.fairygarden.client.render.MoonWispRenderer;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.registry.ModParticles;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client setup on NeoForge: renderers, model layers, particles, sign materials and the biome ambience. The vanilla
 * sign renderers draw the Fairy Garden signs, and block render layers are inferred from texture transparency.
 */
public final class NeoForgeClient {
    private NeoForgeClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(NeoForgeClient::onRegisterRenderers);
        modBus.addListener(NeoForgeClient::onRegisterLayers);
        modBus.addListener(NeoForgeClient::onRegisterParticles);
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> AmbientEffects.tick());
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BUTTERFLY.get(), ButterflyRenderer::new);
        event.registerEntityRenderer(ModEntities.MOON_WISP.get(), MoonWispRenderer::new);
        for (WoodSet set : ModBlocks.WOOD_SETS) {
            event.registerEntityRenderer(set.boatEntity.get(), context -> new BoatRenderer(context, ModModelLayers.boat(set.name, false)));
            event.registerEntityRenderer(set.chestBoatEntity.get(), context -> new BoatRenderer(context, ModModelLayers.boat(set.name, true)));
        }
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.BUTTERFLY, ButterflyModel::createLayer);
        event.registerLayerDefinition(ModModelLayers.MOON_WISP, MoonWispModel::createLayer);
        for (WoodSet set : ModBlocks.WOOD_SETS) {
            event.registerLayerDefinition(ModModelLayers.boat(set.name, false), BoatModel::createBoatModel);
            event.registerLayerDefinition(ModModelLayers.boat(set.name, true), BoatModel::createChestBoatModel);
        }
    }

    private static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.BLOSSOM_PETAL.get(), PetalParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SNOW_PETAL.get(), PetalParticle.Provider::new);
        event.registerSpriteSet(ModParticles.BUTTERFLY.get(), ButterflyParticle.Provider::new);
        event.registerSpriteSet(ModParticles.FAIRY_SPARKLE.get(), sprites -> new GlowParticle.Provider(sprites, GlowParticle.Style.SPARKLE));
        event.registerSpriteSet(ModParticles.MOON_GLIMMER.get(), sprites -> new GlowParticle.Provider(sprites, GlowParticle.Style.GLIMMER));
        event.registerSpriteSet(ModParticles.POLLEN.get(), sprites -> new GlowParticle.Provider(sprites, GlowParticle.Style.POLLEN));
    }
}
