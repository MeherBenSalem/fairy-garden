package com.sugarplumstudios.fairygarden.worldgen.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/**
 * @param petals sampled once per flower, so each giant flower has a single petal colour
 * @param height stem height in blocks
 */
public record GiantFlowerConfiguration(BlockStateProvider stem, BlockStateProvider petals, BlockStateProvider pistil,
                                       IntProvider height) {
    public static final MapCodec<GiantFlowerConfiguration> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStateProvider.DIRECT_CODEC.fieldOf("stem").forGetter(GiantFlowerConfiguration::stem),
            BlockStateProvider.DIRECT_CODEC.fieldOf("petals").forGetter(GiantFlowerConfiguration::petals),
            BlockStateProvider.DIRECT_CODEC.fieldOf("pistil").forGetter(GiantFlowerConfiguration::pistil),
            IntProviders.codec(3, 12).fieldOf("height").forGetter(GiantFlowerConfiguration::height)
    ).apply(instance, GiantFlowerConfiguration::new));
}
