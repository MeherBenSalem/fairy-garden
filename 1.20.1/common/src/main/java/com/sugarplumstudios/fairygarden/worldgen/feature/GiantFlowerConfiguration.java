package com.sugarplumstudios.fairygarden.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/**
 * @param petals sampled once per flower, so each giant flower has a single petal colour
 * @param height stem height in blocks
 */
public record GiantFlowerConfiguration(BlockStateProvider stem, BlockStateProvider petals, BlockStateProvider pistil,
                                       IntProvider height) implements FeatureConfiguration {
    public static final Codec<GiantFlowerConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("stem").forGetter(GiantFlowerConfiguration::stem),
            BlockStateProvider.CODEC.fieldOf("petals").forGetter(GiantFlowerConfiguration::petals),
            BlockStateProvider.CODEC.fieldOf("pistil").forGetter(GiantFlowerConfiguration::pistil),
            IntProvider.codec(3, 12).fieldOf("height").forGetter(GiantFlowerConfiguration::height)
    ).apply(instance, GiantFlowerConfiguration::new));
}
