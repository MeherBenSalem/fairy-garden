package com.sugarplumstudios.fairygarden.datagen.structure;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * A tiny in-code structure builder that serialises to the vanilla structure-template NBT format. Positions that are
 * never set stay "structure void", so the surrounding terrain shows through.
 */
public final class Blueprint {
    public final String name;
    final int sizeX, sizeY, sizeZ;
    final Random random;
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final Map<BlockPos, CompoundTag> blockEntities = new HashMap<>();

    Blueprint(String name, int sizeX, int sizeY, int sizeZ) {
        this.name = name;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.random = new Random(name.hashCode());
    }

    Blueprint set(int x, int y, int z, BlockState state) {
        if (x < 0 || y < 0 || z < 0 || x >= sizeX || y >= sizeY || z >= sizeZ) {
            throw new IllegalArgumentException(name + ": block out of bounds at " + x + "," + y + "," + z);
        }
        BlockPos pos = new BlockPos(x, y, z);
        blocks.put(pos, state);
        blockEntities.remove(pos);
        return this;
    }

    Blueprint set(int x, int y, int z, Block block) {
        return set(x, y, z, block.defaultBlockState());
    }

    Blueprint fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    set(x, y, z, state);
                }
            }
        }
        return this;
    }

    Blueprint fill(int x1, int y1, int z1, int x2, int y2, int z2, Block block) {
        return fill(x1, y1, z1, x2, y2, z2, block.defaultBlockState());
    }

    /** Fills only the positions that have not been set yet. */
    Blueprint fillEmpty(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    if (!blocks.containsKey(new BlockPos(x, y, z))) {
                        set(x, y, z, state);
                    }
                }
            }
        }
        return this;
    }

    Blueprint clearAir(int x1, int y1, int z1, int x2, int y2, int z2) {
        return fillEmpty(x1, y1, z1, x2, y2, z2, Blocks.AIR.defaultBlockState());
    }

    boolean isSet(int x, int y, int z) {
        return blocks.containsKey(new BlockPos(x, y, z));
    }

    /**
     * Covers every still-empty cell that sits on grass or moss at layer {@code y} with a random decoration.
     * Trees generate after structures, so leaving no bare soil keeps saplings from rooting inside the build.
     */
    Blueprint decorateOpenGround(int y, java.util.List<BlockState> decorations) {
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                BlockState below = blocks.get(new BlockPos(x, y - 1, z));
                if (below != null && (below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.MOSS_BLOCK)) && !isSet(x, y, z)) {
                    set(x, y, z, decorations.get(random.nextInt(decorations.size())));
                }
            }
        }
        return this;
    }

    Blueprint chest(int x, int y, int z, Direction facing, ResourceLocation lootTable) {
        set(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "minecraft:chest");
        tag.putString("LootTable", lootTable.toString());
        blockEntities.put(new BlockPos(x, y, z), tag);
        return this;
    }

    CompoundTag toNbt() {
        Map<BlockState, Integer> palette = new LinkedHashMap<>();
        ListTag blockList = new ListTag();
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            int index = palette.computeIfAbsent(entry.getValue(), state -> palette.size());
            BlockPos pos = entry.getKey();
            CompoundTag block = new CompoundTag();
            ListTag posTag = new ListTag();
            posTag.add(IntTag.valueOf(pos.getX()));
            posTag.add(IntTag.valueOf(pos.getY()));
            posTag.add(IntTag.valueOf(pos.getZ()));
            block.put("pos", posTag);
            block.putInt("state", index);
            CompoundTag nbt = blockEntities.get(pos);
            if (nbt != null) {
                block.put("nbt", nbt.copy());
            }
            blockList.add(block);
        }
        ListTag paletteList = new ListTag();
        palette.keySet().forEach(state -> paletteList.add(NbtUtils.writeBlockState(state)));

        ListTag size = new ListTag();
        size.add(IntTag.valueOf(sizeX));
        size.add(IntTag.valueOf(sizeY));
        size.add(IntTag.valueOf(sizeZ));

        CompoundTag root = new CompoundTag();
        root.put("size", size);
        root.put("palette", paletteList);
        root.put("blocks", blockList);
        root.put("entities", new ListTag());
        root.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        return root;
    }
}
