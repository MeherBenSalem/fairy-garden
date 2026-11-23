package com.sugarplumstudios.fairygarden.neoforge;

import com.sugarplumstudios.fairygarden.client.AmbientEffects;
import com.sugarplumstudios.fairygarden.client.particle.ButterflyParticle;
import com.sugarplumstudios.fairygarden.client.particle.GlowParticle;
import com.sugarplumstudios.fairygarden.client.particle.PetalParticle;
import com.sugarplumstudios.fairygarden.client.render.ButterflyModel;
import com.sugarplumstudios.fairygarden.client.render.ButterflyRenderer;
import com.sugarplumstudios.fairygarden.client.render.ModBoatRenderer;
import com.sugarplumstudios.fairygarden.client.render.ModModelLayers;
import com.sugarplumstudios.fairygarden.client.render.MoonWispModel;
import com.sugarplumstudios.fairygarden.client.render.MoonWispRenderer;
import com.sugarplumstudios.fairygarden.entity.BoatWood;
import com.sugarplumstudios.fairygarden.registry.ModBlockEntities;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.registry.ModParticles;
import com.sugarplumstudios.fairygarden.registry.ModWoodTypes;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client setup on NeoForge: renderers, model layers, particles, sign materials and the biome ambience. */
public final class NeoForgeClient {
    private NeoForgeClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(NeoForgeClient::onClientSetup);
        modBus.addListener(NeoForgeClient::onRegisterRenderers);
        modBus.addListener(NeoForgeClient::onRegisterLayers);
        modBus.addListener(NeoForgeClient::onRegisterParticles);
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> AmbientEffects.tick());
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Sheets.addWoodType(ModWoodTypes.BLOSSOM);
            Sheets.addWoodType(ModWoodTypes.MOONWOOD);
        });
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BOAT.get(), context -> new ModBoatRenderer(context, false));
        event.registerEntityRenderer(ModEntities.CHEST_BOAT.get(), context -> new ModBoatRenderer(context, true));
        event.registerEntityRenderer(ModEntities.BUTTERFLY.get(), ButterflyRenderer::new);
        event.registerEntityRenderer(ModEntities.MOON_WISP.get(), MoonWispRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SIGN.get(), SignRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HANGING_SIGN.get(), HangingSignRenderer::new);
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.BUTTERFLY, ButterflyModel::createLayer);
        event.registerLayerDefinition(ModModelLayers.MOON_WISP, MoonWispModel::createLayer);
        for (BoatWood wood : BoatWood.values()) {
            event.registerLayerDefinition(ModModelLayers.boat(wood, false), BoatModel::createBodyModel);
            event.registerLayerDefinition(ModModelLayers.boat(wood, true), ChestBoatModel::createBodyModel);
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
