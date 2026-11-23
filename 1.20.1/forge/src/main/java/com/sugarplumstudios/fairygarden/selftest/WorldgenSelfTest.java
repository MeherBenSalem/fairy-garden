package com.sugarplumstudios.fairygarden.selftest;

import com.mojang.datafixers.util.Pair;
import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.Util;
import com.sugarplumstudios.fairygarden.worldgen.ModBiomes;
import com.sugarplumstudios.fairygarden.worldgen.ModStructures;
import com.sugarplumstudios.fairygarden.worldgen.structure.FlatJigsawStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Headless world-generation check, enabled with {@code -Dfairygarden.selftest=true} (see the {@code runWorldgenTest}
 * Gradle task). On a freshly created world it locates every Fairy Garden biome and structure, generates the chunks
 * there, counts the Fairy Garden blocks that were actually placed, counts structure starts per area, renders a biome
 * map, writes {@code fairygarden-selftest.txt} and stops the server. The report ends with PASS or FAIL. Each check
 * runs in its own server tick.
 */
@Mod.EventBusSubscriber(modid = FairyGarden.MOD_ID)
public final class WorldgenSelfTest {
    private static final int BIOME_SEARCH_RADIUS = 8000;
    private static final int DENSITY_AREA = 8192;

    private WorldgenSelfTest() {
    }

    /** The checks still to run, one per server tick, so no tick comes near the dedicated server's 60 s watchdog. */
    private static final Deque<Runnable> steps = new ArrayDeque<>();
    private static final List<String> report = new ArrayList<>();
    private static boolean pass = true;
    private static CompletableFuture<List<String>> background;

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (!Boolean.getBoolean("fairygarden.selftest")) {
            return;
        }
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        BlockPos origin = level.getSharedSpawnPos();
        report.add("Fairy Garden world-generation self test");
        report.add("Seed: " + level.getSeed() + "   spawn: " + origin.toShortString());
        report.add("");
        report.add("== Biomes ==");
        for (ResourceKey<Biome> biome : ModBiomes.ALL) {
            steps.add(() -> checkBiome(level, origin, biome));
        }
        steps.add(() -> {
            report.add("");
            report.add("== Structures ==");
        });
        for (ResourceKey<Structure> key : ModStructures.ALL) {
            steps.add(() -> checkStructure(level, origin, key));
        }
        steps.add(() -> {
            report.add("");
            report.add("== Structure density (" + DENSITY_AREA + " x " + DENSITY_AREA + " blocks around spawn) ==");
        });
        // Pure noise / structure-layout sampling: done off the server thread, like world generation itself.
        inBackground(() -> List.of(ModStructures.FLOWER_SHRINE_SET, ModStructures.HIDDEN_FAIRY_GARDEN_SET,
                ModStructures.MOONSTONE_CIRCLE_SET, ModStructures.MUSHROOM_COTTAGE_SET).stream()
                .map(key -> structureDensity(level, origin, key)).toList());
        steps.add(() -> {
            report.add("");
            report.add("== Biome map ==");
        });
        inBackground(() -> List.of(writeBiomeMap(level, origin, server.getServerDirectory().toPath().resolve("fairygarden-biome-map.png"))));
        steps.add(() -> finish(server));
    }

    /** Queues work that runs on a background thread; its report lines are added once it is done. */
    private static void inBackground(java.util.function.Supplier<List<String>> work) {
        steps.add(() -> background = CompletableFuture.supplyAsync(work, Util.backgroundExecutor()));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (background != null) {
            if (!background.isDone()) {
                return; // keep ticking normally while the background check runs
            }
            try {
                for (String line : background.join()) {
                    pass &= !line.startsWith("FAIL");
                    report.add(line);
                }
            } catch (CompletionException e) {
                FairyGarden.LOGGER.error("[selftest] a background check crashed", e.getCause());
                report.add("FAIL  a check crashed: " + e.getCause());
                pass = false;
            }
            background = null;
            return;
        }
        if (steps.isEmpty()) {
            return;
        }
        Runnable step = steps.poll();
        try {
            step.run();
        } catch (RuntimeException e) {
            FairyGarden.LOGGER.error("[selftest] a check crashed", e);
            report.add("FAIL  a check crashed: " + e);
            pass = false;
            if (steps.isEmpty()) {
                finish(event.getServer());
            }
        }
    }

    private static void checkBiome(ServerLevel level, BlockPos origin, ResourceKey<Biome> biome) {
        long start = System.currentTimeMillis();
        Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(holder -> holder.is(biome), origin, BIOME_SEARCH_RADIUS, 32, 64);
        if (found == null) {
            report.add("FAIL  " + biome.location() + ": not found within " + BIOME_SEARCH_RADIUS + " blocks");
            pass = false;
            return;
        }
        BlockPos at = found.getFirst();
        int distance = (int) Math.sqrt(at.distSqr(new BlockPos(origin.getX(), at.getY(), origin.getZ())));
        Map<String, Integer> blocks = sampleDecorations(level, at);
        int total = blocks.values().stream().mapToInt(Integer::intValue).sum();
        boolean decorated = total > 0;
        pass &= decorated;
        report.add((decorated ? "OK    " : "FAIL  ") + biome.location() + " at " + at.getX() + " " + at.getZ()
                + " (" + distance + " blocks from spawn, " + (System.currentTimeMillis() - start) + " ms)");
        report.add("      Fairy Garden blocks in the surrounding 5x5 chunks: " + total + " " + blocks);
    }

    private static void checkStructure(ServerLevel level, BlockPos origin, ResourceKey<Structure> key) {
        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Holder<Structure> structure = structures.getHolderOrThrow(key);
        Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator()
                .findNearestMapStructure(level, HolderSet.direct(structure), origin, 100, false);
        if (found == null) {
            report.add("FAIL  " + key.location() + ": not found");
            pass = false;
            return;
        }
        BlockPos at = found.getFirst();
        ChunkPos chunk = new ChunkPos(at);
        // Generate the start chunk and its neighbours so the whole piece is placed.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.getChunk(chunk.x + dx, chunk.z + dz);
            }
        }
        StructureStart start = level.structureManager().getStartForStructure(
                net.minecraft.core.SectionPos.of(at), structure.value(), level.getChunk(chunk.x, chunk.z));
        if (start == null || !start.isValid()) {
            report.add("FAIL  " + key.location() + ": located at " + at.toShortString() + " but no valid start was generated");
            pass = false;
            return;
        }
        BoundingBox box = start.getBoundingBox();
        int chests = 0;
        int fairyBlocks = 0;
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.CHEST)) {
                chests++;
            }
            if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals(FairyGarden.MOD_ID)) {
                fairyBlocks++;
            }
        }
        boolean ok = chests > 0 && fairyBlocks > 0;
        pass &= ok;
        int distance = (int) Math.sqrt(at.distSqr(new BlockPos(origin.getX(), at.getY(), origin.getZ())));
        report.add((ok ? "OK    " : "FAIL  ") + key.location() + " at " + box.getCenter().toShortString() + " (" + distance
                + " blocks from spawn) size " + box.getXSpan() + "x" + box.getYSpan() + "x" + box.getZSpan()
                + ", treasure chests: " + chests + ", Fairy Garden blocks: " + fairyBlocks);
    }

    private static void finish(MinecraftServer server) {
        report.add("");
        report.add(pass ? "RESULT: PASS" : "RESULT: FAIL");
        report.forEach(line -> FairyGarden.LOGGER.info("[selftest] {}", line));
        Path file = server.getServerDirectory().toPath().resolve("fairygarden-selftest.txt");
        try {
            Files.write(file, report, StandardCharsets.UTF_8);
        } catch (IOException e) {
            FairyGarden.LOGGER.error("Could not write the self-test report", e);
        }
        server.halt(false);
    }

    /**
     * Counts the structure starts of a set across the density area by evaluating every placement candidate exactly
     * as world generation does. For {@link FlatJigsawStructure}s it also counts the candidates that were in a
     * fitting biome but rejected for steep or flooded ground.
     */
    private static String structureDensity(ServerLevel level, BlockPos origin, ResourceKey<StructureSet> key) {
        StructureSet set = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).getOrThrow(key);
        if (!(set.placement() instanceof RandomSpreadStructurePlacement placement)) {
            return "FAIL  " + key.location() + ": unexpected placement " + set.placement().type();
        }
        Structure structure = set.structures().get(0).structure().value();
        Structure layout = structure instanceof FlatJigsawStructure flat ? flat.layout() : structure;
        int half = DENSITY_AREA / 32;
        ChunkPos centre = new ChunkPos(origin);
        Map<FlatJigsawStructure.Ground, Integer> outcomes = new java.util.EnumMap<>(FlatJigsawStructure.Ground.class);
        int inBiome = 0;
        int regions = 0;
        for (int rx = Math.floorDiv(centre.x - half, placement.spacing()); rx <= Math.floorDiv(centre.x + half, placement.spacing()); rx++) {
            for (int rz = Math.floorDiv(centre.z - half, placement.spacing()); rz <= Math.floorDiv(centre.z + half, placement.spacing()); rz++) {
                // Takes any chunk inside the placement cell and returns that cell's candidate chunk.
                ChunkPos candidate = placement.getPotentialStructureChunk(level.getSeed(), rx * placement.spacing(), rz * placement.spacing());
                if (Math.abs(candidate.x - centre.x) > half || Math.abs(candidate.z - centre.z) > half) {
                    continue;
                }
                regions++;
                var stub = layout.findValidGenerationPoint(context(level, candidate, layout));
                if (stub.isPresent()) {
                    inBiome++;
                    FlatJigsawStructure.Ground ground = structure instanceof FlatJigsawStructure flat
                            ? flat.ground(context(level, candidate, structure), stub.get().getPiecesBuilder().getBoundingBox())
                            : FlatJigsawStructure.Ground.SUITABLE;
                    outcomes.merge(ground, 1, Integer::sum);
                }
            }
        }
        int generated = outcomes.getOrDefault(FlatJigsawStructure.Ground.SUITABLE, 0);
        return (generated > 0 ? "OK    " : "FAIL  ") + key.location() + ": " + generated + " generated (" + inBiome
                + " candidates in a fitting biome; rejected " + outcomes.getOrDefault(FlatJigsawStructure.Ground.STEEP, 0)
                + " steep, " + outcomes.getOrDefault(FlatJigsawStructure.Ground.FLOODED, 0) + " flooded; "
                + regions + " placement cells, spacing " + placement.spacing() + ")";
    }

    private static Structure.GenerationContext context(ServerLevel level, ChunkPos chunk, Structure structure) {
        var generator = level.getChunkSource().getGenerator();
        return new Structure.GenerationContext(level.registryAccess(), generator, generator.getBiomeSource(),
                level.getChunkSource().randomState(), level.getServer().getStructureManager(), level.getSeed(), chunk, level,
                structure.biomes()::contains);
    }

    /**
     * Renders an 8192 x 8192 block top-down biome map around spawn (8 blocks per pixel) sampled at sea level.
     * Fairy Garden biomes are drawn in their signature colours; vanilla biomes in muted greys/blues.
     */
    private static String writeBiomeMap(ServerLevel level, BlockPos center, Path file) {
        int size = 1024;
        int step = 8;
        var source = level.getChunkSource().getGenerator().getBiomeSource();
        var sampler = level.getChunkSource().randomState().sampler();
        Map<ResourceKey<Biome>, Integer> colours = Map.of(
                ModBiomes.BLOSSOM_VALLEY, 0xF29BBF, ModBiomes.LAVENDER_FIELDS, 0xB996EC, ModBiomes.STRAWBERRY_MEADOW, 0xE85468,
                ModBiomes.MOONLIGHT_FOREST, 0x3F62B4, ModBiomes.FAIRY_GLADE, 0x52D6A0);
        Map<String, Integer> area = new TreeMap<>();
        int[] rgb = new int[size * size];
        for (int py = 0; py < size; py++) {
            for (int px = 0; px < size; px++) {
                int x = center.getX() + (px - size / 2) * step;
                int z = center.getZ() + (py - size / 2) * step;
                Holder<Biome> biome = source.getNoiseBiome(x >> 2, 63 >> 2, z >> 2, sampler);
                ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
                Integer colour = key == null ? null : colours.get(key);
                if (colour == null) {
                    String path = key == null ? "" : key.location().getPath();
                    colour = path.contains("ocean") || path.contains("river") ? 0x6C7FA6
                            : path.contains("snow") || path.contains("frozen") || path.contains("ice") ? 0xD8DEE8
                            : path.contains("desert") || path.contains("badlands") || path.contains("beach") ? 0xCBBE96
                            : path.contains("forest") || path.contains("taiga") || path.contains("jungle") ? 0x5E7A5C : 0x8A9A80;
                } else {
                    area.merge(key.location().getPath(), 1, Integer::sum);
                }
                rgb[py * size + px] = colour;
            }
        }
        for (int i = -6; i <= 6; i++) {
            rgb[(size / 2) * size + size / 2 + i] = 0xFFFFFF;
            rgb[(size / 2 + i) * size + size / 2] = 0xFFFFFF;
        }
        try {
            Files.write(file, png(size, size, rgb));
        } catch (IOException e) {
            return "could not write map: " + e;
        }
        StringBuilder text = new StringBuilder("Wrote " + file.getFileName() + " (" + size * step + " x " + size * step
                + " blocks around spawn). Share of the map per Fairy Garden biome:");
        area.forEach((name, pixels) -> text.append(String.format(" %s %.1f%%", name, 100.0 * pixels / (size * size))));
        return text.toString();
    }

    private static byte[] png(int width, int height, int[] rgb) throws IOException {
        java.io.ByteArrayOutputStream raw = new java.io.ByteArrayOutputStream();
        for (int y = 0; y < height; y++) {
            raw.write(0);
            for (int x = 0; x < width; x++) {
                int c = rgb[y * width + x];
                raw.write(c >> 16 & 0xFF);
                raw.write(c >> 8 & 0xFF);
                raw.write(c & 0xFF);
            }
        }
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        out.write(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});
        java.nio.ByteBuffer header = java.nio.ByteBuffer.allocate(13).putInt(width).putInt(height).put((byte) 8).put((byte) 2)
                .put((byte) 0).put((byte) 0).put((byte) 0);
        chunk(out, "IHDR", header.array());
        java.io.ByteArrayOutputStream compressed = new java.io.ByteArrayOutputStream();
        try (java.util.zip.DeflaterOutputStream deflate = new java.util.zip.DeflaterOutputStream(compressed)) {
            raw.writeTo(deflate);
        }
        chunk(out, "IDAT", compressed.toByteArray());
        chunk(out, "IEND", new byte[0]);
        return out.toByteArray();
    }

    private static void chunk(java.io.ByteArrayOutputStream out, String type, byte[] data) throws IOException {
        byte[] tag = type.getBytes(StandardCharsets.US_ASCII);
        out.write(java.nio.ByteBuffer.allocate(4).putInt(data.length).array());
        out.write(tag);
        out.write(data);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(tag);
        crc.update(data);
        out.write(java.nio.ByteBuffer.allocate(4).putInt((int) crc.getValue()).array());
    }

    /** Generates the 5x5 chunks around {@code center} and counts Fairy Garden blocks near the surface of the biome. */
    private static Map<String, Integer> sampleDecorations(ServerLevel level, BlockPos center) {
        Map<String, Integer> counts = new TreeMap<>();
        ChunkPos middle = new ChunkPos(center);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int cx = middle.x - 2; cx <= middle.x + 2; cx++) {
            for (int cz = middle.z - 2; cz <= middle.z + 2; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                        for (int y = top + 1; y >= top - 30; y--) {
                            pos.set(chunk.getPos().getMinBlockX() + x, y, chunk.getPos().getMinBlockZ() + z);
                            BlockState state = chunk.getBlockState(pos);
                            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                            if (id.getNamespace().equals(FairyGarden.MOD_ID)) {
                                counts.merge(id.getPath(), 1, Integer::sum);
                            }
                        }
                    }
                }
            }
        }
        return counts;
    }
}
