package com.sugarplumstudios.fairygarden.client.dev;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;

/**
 * Server-side world lookups shared by the development-only tours ({@link DevSnapshots}, {@link ShowcaseTour}).
 * Every method must run on the server thread.
 */
final class DevWorld {
    private DevWorld() {
    }

    /** Generates the column if needed and returns the y of its surface for the given heightmap. */
    static int surfaceY(ServerLevel level, int x, int z, Heightmap.Types type) {
        level.getChunk(x >> 4, z >> 4);
        return level.getHeight(type, x, z);
    }

    /** The highest surface (leaves and water included) on a circle around a column, generating it as needed. */
    static int ringTop(ServerLevel level, int x, int z, double radius) {
        int top = level.getMinBuildHeight();
        for (int k = 0; k < 16; k++) {
            double a = k * Math.PI / 8;
            top = Math.max(top, surfaceY(level, x + (int) Math.round(Math.cos(a) * radius),
                    z + (int) Math.round(Math.sin(a) * radius), Heightmap.Types.MOTION_BLOCKING));
        }
        return top;
    }

    /** Points a camera should see: the floor's centre, the top, and four outer corners at mid-height. */
    private static List<Vec3> landmarks(BoundingBox box) {
        BlockPos c = box.getCenter();
        double r = Math.max(box.getXSpan(), box.getZSpan()) / 2.0 * 0.8;
        double mid = box.minY() + box.getYSpan() / 2.0;
        double x = c.getX() + 0.5;
        double z = c.getZ() + 0.5;
        return List.of(new Vec3(x, box.minY() + 1.5, z), new Vec3(x, box.maxY() - 0.5, z),
                new Vec3(x + r, mid, z + r), new Vec3(x - r, mid, z + r), new Vec3(x + r, mid, z - r), new Vec3(x - r, mid, z - r));
    }

    private static boolean blocksView(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).getVisualShape(level, pos, CollisionContext.empty()).isEmpty();
    }

    /**
     * Walks the ray in quarter-block steps and reports whether the structure is what the camera sees along it: the
     * target is reached, or the first block in the way is part of the structure. Flowers and grass (no shape) never
     * block the ray; leaves, trunks and giant flowers do.
     */
    private static boolean seesStructure(ServerLevel level, Vec3 from, Vec3 to, BoundingBox box) {
        BlockPos goal = BlockPos.containing(to);
        int steps = (int) Math.ceil(from.distanceTo(to) / 0.25);
        BlockPos last = null;
        for (int i = 1; i < steps; i++) {
            BlockPos pos = BlockPos.containing(from.lerp(to, (double) i / steps));
            if (pos.equals(last)) {
                continue;
            }
            last = pos;
            if (pos.equals(goal)) {
                return true;
            }
            if (blocksView(level, pos)) {
                return box.isInside(pos);
            }
        }
        return true;
    }

    /** How many of the structure's landmarks are in plain sight from the eye, or -1 if the eye is inside a block. */
    static int visibility(ServerLevel level, Vec3 eye, BoundingBox box) {
        if (blocksView(level, BlockPos.containing(eye))) {
            return -1;
        }
        int seen = 0;
        for (Vec3 landmark : landmarks(box)) {
            if (seesStructure(level, eye, landmark, box)) {
                seen++;
            }
        }
        return seen;
    }

    /** How many blocks with a shape surround the eye (3x3x3): anything that close fills half the picture. */
    private static int crowding(ServerLevel level, Vec3 eye) {
        BlockPos centre = BlockPos.containing(eye);
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-1, -1, -1), centre.offset(1, 1, 1))) {
            if (blocksView(level, pos)) {
                count++;
            }
        }
        return count;
    }

    /**
     * The clearest still camera (eye position) for a structure, trying sixteen directions and several distances and
     * heights in order of preference: every landmark in sight, nothing right in front of the lens. {@code clearing}
     * keeps the camera inside the footprint, for structures roofed by forest canopy all around.
     */
    static Vec3 viewpoint(ServerLevel level, BoundingBox box, boolean clearing) {
        BlockPos c = box.getCenter();
        double half = Math.max(box.getXSpan(), box.getZSpan()) / 2.0;
        double[] radii = clearing ? new double[]{half - 1.5, half - 2.5, half + 1} : new double[]{half + 9, half + 6, half + 13};
        double[] heights = clearing ? new double[]{1.5, 0.5, 2.5, -0.5, 4} : new double[]{5, 8, 3, 12};
        int perfect = landmarks(box).size() * 10;
        Vec3 best = null;
        int bestScore = Integer.MIN_VALUE;
        for (double radius : radii) {
            int base = clearing ? box.minY() + box.getYSpan() : Math.max(box.maxY(), ringTop(level, c.getX(), c.getZ(), radius));
            for (double height : heights) {
                for (int k = 0; k < 16; k++) {
                    // Start between the diagonals: ring-shaped builds put their stones or posts on them.
                    double a = Math.PI / 8 * (k + 0.5) + Math.PI / 4;
                    Vec3 eye = new Vec3(c.getX() + 0.5 + Math.cos(a) * radius, base + height, c.getZ() + 0.5 + Math.sin(a) * radius);
                    int seen = visibility(level, eye, box);
                    if (seen < 0) {
                        continue;
                    }
                    int score = seen * 10 - crowding(level, eye);
                    if (score > bestScore) {
                        bestScore = score;
                        best = eye;
                        if (score == perfect) {
                            return best;
                        }
                    }
                }
            }
        }
        return best != null ? best : new Vec3(c.getX() + 0.5 + half + 9, box.maxY() + 8, c.getZ() + 0.5 + half + 9);
    }

    /** For a camera circling inside a clearing: the radius and eye height whose whole circle has the clearest views. */
    static double[] clearingOrbit(ServerLevel level, BoundingBox box) {
        BlockPos c = box.getCenter();
        double half = Math.max(box.getXSpan(), box.getZSpan()) / 2.0;
        double[] best = {half - 1.5, box.minY() + box.getYSpan() + 1.5};
        int bestScore = Integer.MIN_VALUE;
        for (double radius : new double[]{half - 1.5, half - 2.5, half - 3.5}) {
            for (double height : new double[]{1.5, 0.5, 2.5, -0.5}) {
                double eyeY = box.minY() + box.getYSpan() + height;
                int score = 0;
                for (int k = 0; k < 16; k++) {
                    double a = Math.PI / 8 * k;
                    Vec3 eye = new Vec3(c.getX() + 0.5 + Math.cos(a) * radius, eyeY, c.getZ() + 0.5 + Math.sin(a) * radius);
                    int seen = visibility(level, eye, box);
                    score += seen < 0 ? -50 : seen * 10 - crowding(level, eye);
                }
                if (score > bestScore) {
                    bestScore = score;
                    best = new double[]{radius, eyeY};
                }
            }
        }
        return best;
    }

    private static boolean surfaceIs(ServerLevel level, ResourceKey<Biome> biome, int x, int z) {
        int y = level.getChunkSource().getGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level,
                level.getChunkSource().randomState());
        return level.getBiome(new BlockPos(x, y, z)).is(biome);
    }

    /** From the closest point of a biome, walks to a spot whose surface and surroundings are that biome. */
    private static BlockPos deepInBiome(ServerLevel level, BlockPos found, ResourceKey<Biome> biome) {
        BlockPos best = found;
        int bestScore = -1;
        for (int r = 0; r <= 120; r += 24) {
            for (int a = 0; a < (r == 0 ? 1 : 8); a++) {
                int x = found.getX() + (int) (Math.cos(a * Math.PI / 4) * r);
                int z = found.getZ() + (int) (Math.sin(a * Math.PI / 4) * r);
                if (!surfaceIs(level, biome, x, z)) {
                    continue;
                }
                int score = 0;
                for (int k = 0; k < 8; k++) {
                    if (surfaceIs(level, biome, x + (int) (Math.cos(k * Math.PI / 4) * 24), z + (int) (Math.sin(k * Math.PI / 4) * 24))) {
                        score++;
                    }
                }
                if (score > bestScore) {
                    bestScore = score;
                    best = new BlockPos(x, found.getY(), z);
                }
            }
        }
        return best;
    }

    /** The nearest spot well inside the biome, or {@code null} if there is none within 6400 blocks. */
    static BlockPos locateBiome(ServerLevel level, ResourceKey<Biome> biome, BlockPos origin) {
        Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(h -> h.is(biome), origin, 6400, 32, 64);
        return found == null ? null : deepInBiome(level, found.getFirst(), biome);
    }

    /** The locate position of the nearest structure start, or {@code null} if there is none within 100 chunks. */
    static BlockPos locateStructure(ServerLevel level, ResourceKey<Structure> key, BlockPos origin) {
        Holder<Structure> holder = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolderOrThrow(key);
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder), origin, 100, false);
        return found == null ? null : found.getFirst();
    }

    /** Generates the chunks around a located structure and returns its bounding box, or {@code null}. */
    static BoundingBox structureBox(ServerLevel level, ResourceKey<Structure> key, BlockPos located) {
        Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(key);
        ChunkPos chunk = new ChunkPos(located);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.getChunk(chunk.x + dx, chunk.z + dz);
            }
        }
        StructureStart start = level.structureManager().getStartForStructure(SectionPos.of(located), structure,
                level.getChunk(chunk.x, chunk.z));
        if (start == null || !start.isValid() || start.getPieces().isEmpty()) {
            return null;
        }
        return start.getPieces().get(0).getBoundingBox();
    }
}
