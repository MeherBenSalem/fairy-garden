package com.sugarplumstudios.fairygarden.fabric;

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
import com.sugarplumstudios.fairygarden.config.FairyConfig;
import com.sugarplumstudios.fairygarden.entity.BoatWood;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;
import com.sugarplumstudios.fairygarden.registry.ModBlockEntities;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.Block;

public final class FairyGardenFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FairyConfig.loadClient();

        EntityRendererRegistry.register(ModEntities.BOAT.get(), context -> new ModBoatRenderer(context, false));
        EntityRendererRegistry.register(ModEntities.CHEST_BOAT.get(), context -> new ModBoatRenderer(context, true));
        EntityRendererRegistry.register(ModEntities.BUTTERFLY.get(), ButterflyRenderer::new);
        EntityRendererRegistry.register(ModEntities.MOON_WISP.get(), MoonWispRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.SIGN.get(), SignRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.HANGING_SIGN.get(), HangingSignRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.BUTTERFLY, ButterflyModel::createLayer);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.MOON_WISP, MoonWispModel::createLayer);
        for (BoatWood wood : BoatWood.values()) {
            EntityModelLayerRegistry.registerModelLayer(ModModelLayers.boat(wood, false), BoatModel::createBodyModel);
            EntityModelLayerRegistry.registerModelLayer(ModModelLayers.boat(wood, true), ChestBoatModel::createBodyModel);
        }

        ParticleFactoryRegistry particles = ParticleFactoryRegistry.getInstance();
        particles.register(ModParticles.BLOSSOM_PETAL.get(), PetalParticle.Provider::new);
        particles.register(ModParticles.SNOW_PETAL.get(), PetalParticle.Provider::new);
        particles.register(ModParticles.BUTTERFLY.get(), ButterflyParticle.Provider::new);
        particles.register(ModParticles.FAIRY_SPARKLE.get(), sprites -> new GlowParticle.Provider(sprites, GlowParticle.Style.SPARKLE));
        particles.register(ModParticles.MOON_GLIMMER.get(), sprites -> new GlowParticle.Provider(sprites, GlowParticle.Style.GLIMMER));
        particles.register(ModParticles.POLLEN.get(), sprites -> new GlowParticle.Provider(sprites, GlowParticle.Style.POLLEN));

        // Forge reads the render type from each block model ("render_type"); Fabric needs it per block.
        for (RegistryEntry<? extends Block> entry : ModBlocks.ALL) {
            String name = entry.getId().getPath();
            if (FabricRenderLayers.CUTOUT_MIPPED.contains(name)) {
                BlockRenderLayerMap.INSTANCE.putBlock(entry.get(), RenderType.cutoutMipped());
            } else if (FabricRenderLayers.CUTOUT.contains(name)) {
                BlockRenderLayerMap.INSTANCE.putBlock(entry.get(), RenderType.cutout());
            }
        }

        // Sign textures need no call here: the wood types are registered while the mod initialises, before the sign
        // materials are built, and Fabric API resolves their namespaced texture paths (Forge uses Sheets.addWoodType).
        ClientTickEvents.END_CLIENT_TICK.register(client -> AmbientEffects.tick());
    }
}
