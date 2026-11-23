package com.sugarplumstudios.fairygarden.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sugarplumstudios.fairygarden.registry.ModStructureTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.Optional;

/**
 * A jigsaw structure that only generates on dry, fairly level ground. The terrain under the footprint may vary by
 * at most {@code max_height_difference} blocks. Without this check, a small building on a hillside sits in a
 * square pit, because terrain adaptation flattens the uphill side of its footprint.
 * <p>
 * The wrapped {@code jigsaw} does the actual layout. This type adds the terrain check. Rejected candidates
 * generate nothing, and {@code /locate} skips them because it evaluates starts the same way.
 */
public final class FlatJigsawStructure extends Structure {
    public static final MapCodec<FlatJigsawStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            JigsawStructure.CODEC.codec().fieldOf("jigsaw").forGetter(s -> s.jigsaw),
            Codec.intRange(0, 64).fieldOf("max_height_difference").forGetter(s -> s.maxHeightDifference)
    ).apply(instance, FlatJigsawStructure::new));

    private final JigsawStructure jigsaw;
    private final int maxHeightDifference;

    public FlatJigsawStructure(StructureSettings settings, JigsawStructure jigsaw, int maxHeightDifference) {
        super(settings);
        this.jigsaw = jigsaw;
        this.maxHeightDifference = maxHeightDifference;
    }

    /** The wrapped layout, without the ground check (used by the self-test to count rejected candidates). */
    public JigsawStructure layout() {
        return jigsaw;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        Optional<GenerationStub> found = jigsaw.findGenerationPoint(context);
        if (found.isEmpty()) {
            return found;
        }
        StructurePiecesBuilder pieces = found.get().getPiecesBuilder();
        if (pieces.isEmpty() || ground(context, pieces.getBoundingBox()) != Ground.SUITABLE) {
            return Optional.empty();
        }
        // Keep the pieces that were already laid out rather than building them a second time.
        return Optional.of(new GenerationStub(found.get().position(), Either.right(pieces)));
    }

    public enum Ground { SUITABLE, STEEP, FLOODED }

    /** Samples the footprint's corners, edge midpoints and centre: level enough, and not under water. */
    public Ground ground(GenerationContext context, BoundingBox box) {
        int[] xs = {box.minX(), (box.minX() + box.maxX()) / 2, box.maxX()};
        int[] zs = {box.minZ(), (box.minZ() + box.maxZ()) / 2, box.maxZ()};
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        for (int x : xs) {
            for (int z : zs) {
                int surface = height(context, x, z, Heightmap.Types.WORLD_SURFACE_WG);
                if (surface - height(context, x, z, Heightmap.Types.OCEAN_FLOOR_WG) > 0) {
                    return Ground.FLOODED;
                }
                lowest = Math.min(lowest, surface);
                highest = Math.max(highest, surface);
            }
        }
        return highest - lowest > maxHeightDifference ? Ground.STEEP : Ground.SUITABLE;
    }

    private static int height(GenerationContext context, int x, int z, Heightmap.Types type) {
        return context.chunkGenerator().getBaseHeight(x, z, type, context.heightAccessor(), context.randomState());
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.FLAT_JIGSAW.get();
    }
}
