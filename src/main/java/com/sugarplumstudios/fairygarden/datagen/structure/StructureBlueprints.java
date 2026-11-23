package com.sugarplumstudios.fairygarden.datagen.structure;

import com.sugarplumstudios.fairygarden.block.StrawberryBushBlock;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModLootTables;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.List;

/** The hand-designed Fairy Garden structures. Each template's bottom layer sits at ground level. */
public final class StructureBlueprints {
    private StructureBlueprints() {
    }

    public static List<Blueprint> all() {
        return List.of(
                flowerShrine("flower_shrine_blossom", ModBlocks.BLOSSOM.log.get().defaultBlockState(), ModBlocks.BLOSSOM.planks.get(),
                        ModBlocks.BLOSSOM.stairs.get(), ModBlocks.BLOSSOM.slab.get(), ModBlocks.BLOSSOM_LEAVES.get(),
                        new Block[]{ModBlocks.ROSY_DAISY.get(), ModBlocks.CREAM_BUTTERCUP.get(), ModBlocks.STARFLOWER.get(), ModBlocks.MINT_POSY.get()},
                        ModBlocks.BLOSSOM_PETALS.get(), ModBlocks.FAIRY_LANTERN.get()),
                flowerShrine("flower_shrine_lavender", ModBlocks.LAVENDER_BALE.get().defaultBlockState(), ModBlocks.MOONWOOD.planks.get(),
                        ModBlocks.MOONWOOD.stairs.get(), ModBlocks.MOONWOOD.slab.get(), ModBlocks.SNOW_BLOSSOM_LEAVES.get(),
                        new Block[]{ModBlocks.LAVENDER.get(), ModBlocks.BLUEBELL.get(), ModBlocks.STARFLOWER.get(), ModBlocks.LAVENDER.get()},
                        ModBlocks.SNOW_BLOSSOM_PETALS.get(), ModBlocks.MOON_LANTERN.get()),
                hiddenFairyGarden(),
                moonstoneCircle(),
                mushroomCottage());
    }

    // ------------------------------------------------------------------------------------------ flower shrine

    /** A little roofed pavilion with four flower beds, a crystal-flower altar and a chest behind it. */
    private static Blueprint flowerShrine(String name, BlockState pillar, Block planks, Block stairs, Block slab, Block drape,
                                          Block[] beds, Block petals, Block lantern) {
        Blueprint b = new Blueprint(name, 9, 9, 9);
        b.fill(0, 0, 0, 8, 0, 8, Blocks.GRASS_BLOCK);
        b.fill(1, 0, 1, 7, 0, 7, Blocks.STONE_BRICKS);
        b.fill(2, 0, 2, 6, 0, 6, Blocks.CALCITE);
        b.set(4, 0, 8, Blocks.DIRT_PATH);

        int[][] bedSpots = {{2, 2}, {6, 2}, {2, 6}, {6, 6}};
        for (int i = 0; i < bedSpots.length; i++) {
            b.set(bedSpots[i][0], 0, bedSpots[i][1], Blocks.GRASS_BLOCK);
            b.set(bedSpots[i][0], 1, bedSpots[i][1], beds[i]);
        }
        for (int[] corner : new int[][]{{1, 1}, {7, 1}, {1, 7}, {7, 7}}) {
            b.fill(corner[0], 1, corner[1], corner[0], 4, corner[1], pillar);
        }
        b.set(4, 1, 4, Blocks.QUARTZ_PILLAR);
        b.set(4, 2, 4, ModBlocks.POTTED.get(ModBlocks.CRYSTAL_FLOWER).get());
        b.chest(4, 1, 6, Direction.NORTH, ModLootTables.FLOWER_SHRINE);
        b.set(3, 1, 8, ModBlocks.BLOSSOM.fence.get());
        b.set(5, 1, 8, ModBlocks.BLOSSOM.fence.get());
        b.set(3, 2, 8, lantern);
        b.set(5, 2, 8, lantern);
        b.set(4, 4, 4, lantern.defaultBlockState().setValue(LanternBlock.HANGING, true));

        for (int x = 2; x <= 6; x++) {
            for (int z = 2; z <= 6; z++) {
                if (!b.isSet(x, 1, z) && b.random.nextInt(10) < 3) {
                    b.set(x, 1, z, petalState(b, petals));
                }
            }
        }
        BlockState leaves = drape.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int[] corner : new int[][]{{1, 1}, {7, 1}, {1, 7}, {7, 7}}) {
            int ox = corner[0] == 1 ? 0 : 8;
            int oz = corner[1] == 1 ? 0 : 8;
            b.set(ox, 4, corner[1], leaves);
            b.set(corner[0], 4, oz, leaves);
            if (b.random.nextBoolean()) {
                b.set(ox, 3, corner[1], leaves);
            }
        }
        b.decorateOpenGround(1, meadowDecor());
        b.clearAir(0, 1, 0, 8, 4, 8);

        roofTier(b, 5, 0, 8, planks, stairs, slab);
        roofTier(b, 6, 1, 7, planks, stairs, slab);
        roofTier(b, 7, 2, 6, planks, stairs, slab);
        b.set(4, 8, 4, ModBlocks.FAIRY_CRYSTAL_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP));
        b.set(3, 8, 4, leaves);
        b.set(5, 8, 3, leaves);
        return b;
    }

    private static void roofTier(Blueprint b, int y, int lo, int hi, Block planks, Block stairs, Block slab) {
        for (int x = lo; x <= hi; x++) {
            for (int z = lo; z <= hi; z++) {
                boolean edgeX = x == lo || x == hi;
                boolean edgeZ = z == lo || z == hi;
                if (edgeX && edgeZ) {
                    b.set(x, y, z, slab);
                } else if (z == lo) {
                    b.set(x, y, z, stairs.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
                } else if (z == hi) {
                    b.set(x, y, z, stairs.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
                } else if (x == lo) {
                    b.set(x, y, z, stairs.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
                } else if (x == hi) {
                    b.set(x, y, z, stairs.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
                } else {
                    b.set(x, y, z, planks);
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------ hidden fairy garden

    /**
     * A walled secret garden: flowering hedges, an arched gate, two giant flowers, a lily pond, lantern posts,
     * a crystal nook and a chest tucked behind hydrangeas.
     */
    private static Blueprint hiddenFairyGarden() {
        Blueprint b = new Blueprint("hidden_fairy_garden", 13, 7, 13);
        b.fill(0, 0, 0, 12, 0, 12, Blocks.GRASS_BLOCK);
        for (int x = 4; x <= 8; x++) {
            for (int z = 4; z <= 8; z++) {
                b.set(x, 0, z, Blocks.MOSS_BLOCK);
            }
        }
        b.fill(5, 0, 5, 7, 0, 7, Blocks.WATER);
        for (int z = 9; z <= 12; z++) {
            b.set(6, 0, z, Blocks.DIRT_PATH);
        }

        // Hedge walls with an opening for the gate.
        for (int x = 0; x <= 12; x++) {
            for (int z = 0; z <= 12; z++) {
                boolean perimeter = x == 0 || x == 12 || z == 0 || z == 12;
                if (!perimeter || (z == 12 && x >= 4 && x <= 8)) {
                    continue;
                }
                for (int y = 1; y <= 3; y++) {
                    b.set(x, y, z, hedge(b));
                }
            }
        }
        BlockState post = ModBlocks.BLOSSOM.log.get().defaultBlockState();
        for (int[] corner : new int[][]{{0, 0}, {12, 0}, {0, 12}, {12, 12}}) {
            b.fill(corner[0], 1, corner[1], corner[0], 4, corner[1], post);
            b.set(corner[0], 5, corner[1], ModBlocks.FAIRY_LANTERN.get());
        }
        // Gate arch
        b.fill(4, 1, 12, 4, 4, 12, post);
        b.fill(8, 1, 12, 8, 4, 12, post);
        b.fill(5, 4, 12, 7, 4, 12, post.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        BlockState blossom = ModBlocks.BLOSSOM_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int x = 4; x <= 8; x++) {
            b.set(x, 5, 12, blossom);
        }
        b.set(5, 3, 12, blossom);
        b.set(7, 3, 12, blossom);

        // Two giant flowers in the back corners.
        giantFlower(b, 2, 2, 3, ModBlocks.PINK_GIANT_PETALS.get());
        giantFlower(b, 10, 2, 4, ModBlocks.LAVENDER_GIANT_PETALS.get());

        // Pond life
        b.set(5, 1, 6, ModBlocks.GLIMMER_LILY.get());
        b.set(7, 1, 5, ModBlocks.GLIMMER_LILY.get());
        b.set(6, 1, 7, Blocks.LILY_PAD);
        b.set(4, 1, 4, ModBlocks.GLOWCAP.get());
        b.set(8, 1, 8, ModBlocks.GLOWCAP.get());
        b.set(8, 1, 4, ModBlocks.FAIRY_MUSHROOM.get());
        b.set(4, 1, 8, ModBlocks.FAIRY_MUSHROOM.get());

        // Flower borders along the side hedges.
        Block[] border = {ModBlocks.ROSY_DAISY.get(), ModBlocks.BLUEBELL.get(), ModBlocks.MINT_POSY.get(), ModBlocks.STARFLOWER.get(),
                ModBlocks.CREAM_BUTTERCUP.get(), ModBlocks.LAVENDER.get()};
        for (int z = 4; z <= 9; z++) {
            for (int x : new int[]{1, 11}) {
                if (z == 6) {
                    b.set(x, 1, z, ModBlocks.FAIRY_FOXGLOVE.get().defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
                    b.set(x, 2, z, ModBlocks.FAIRY_FOXGLOVE.get().defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
                } else {
                    b.set(x, 1, z, border[b.random.nextInt(border.length)]);
                }
            }
        }

        // Bench facing the pond, and the treasure tucked behind hydrangeas.
        b.set(8, 1, 10, ModBlocks.BLOSSOM.stairs.get().defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
        b.set(9, 1, 10, ModBlocks.BLOSSOM.stairs.get().defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
        b.chest(11, 1, 11, Direction.WEST, ModLootTables.HIDDEN_FAIRY_GARDEN);
        b.set(10, 1, 11, ModBlocks.PINK_HYDRANGEA.get());
        b.set(11, 1, 10, ModBlocks.BLUE_HYDRANGEA.get());
        b.set(10, 1, 10, ModBlocks.LAVENDER_HYDRANGEA.get());

        // Crystal nook
        b.set(1, 1, 11, ModBlocks.FAIRY_CRYSTAL_BLOCK.get());
        b.set(1, 2, 11, cluster(Direction.UP));
        b.set(2, 1, 11, cluster(Direction.UP));
        b.set(1, 1, 10, cluster(Direction.UP));
        b.set(2, 1, 10, ModBlocks.CRYSTAL_FLOWER.get());
        b.set(3, 1, 11, ModBlocks.CRYSTAL_FLOWER.get());

        // Scatter flower carpets over the open lawn.
        List<Block> carpets = List.of(ModBlocks.PINK_FLOWER_CARPET.get(), ModBlocks.MINT_FLOWER_CARPET.get(), ModBlocks.CREAM_FLOWER_CARPET.get(),
                ModBlocks.BLUE_FLOWER_CARPET.get());
        for (int x = 1; x <= 11; x++) {
            for (int z = 1; z <= 11; z++) {
                if (!b.isSet(x, 1, z) && x != 6 && b.random.nextInt(10) < 3) {
                    b.set(x, 1, z, carpets.get(b.random.nextInt(carpets.size())));
                }
            }
        }
        b.decorateOpenGround(1, meadowDecor());
        b.clearAir(0, 1, 0, 12, 6, 12);
        return b;
    }

    private static BlockState hedge(Blueprint b) {
        int roll = b.random.nextInt(10);
        Block leaves = roll < 4 ? Blocks.FLOWERING_AZALEA_LEAVES : roll < 8 ? Blocks.AZALEA_LEAVES : ModBlocks.BLOSSOM_LEAVES.get();
        return leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    }

    private static void giantFlower(Blueprint b, int x, int z, int height, Block petals) {
        b.fill(x, 1, z, x, height, z, ModBlocks.GIANT_FLOWER_STEM.get());
        int top = height + 1;
        b.set(x, top, z, ModBlocks.GIANT_FLOWER_PISTIL.get());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    b.set(x + dx, top, z + dz, petals);
                }
            }
        }
        b.set(x + 1, 1 + height / 2, z, Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
    }

    // ------------------------------------------------------------------------------------------ moonstone circle

    /**
     * A ring of pale standing stones crowned with moon lanterns and crystals around a quartz altar ringed by
     * moonflowers. The bottom layer is underground: the treasure chest is buried beneath the altar.
     */
    private static Blueprint moonstoneCircle() {
        Blueprint b = new Blueprint("moonstone_circle", 11, 6, 11);
        int c = 5;
        b.fill(4, 0, 4, 6, 0, 6, Blocks.COBBLESTONE);
        b.chest(c, 0, c, Direction.NORTH, ModLootTables.MOONSTONE_CIRCLE);

        for (int x = 0; x <= 10; x++) {
            for (int z = 0; z <= 10; z++) {
                double d = Math.sqrt((x - c) * (x - c) + (z - c) * (z - c));
                if (d > 5.4) {
                    continue;
                }
                BlockState ground;
                if (d <= 1.5) {
                    ground = Blocks.SMOOTH_QUARTZ.defaultBlockState();
                } else if (d <= 4.6) {
                    int roll = b.random.nextInt(4);
                    ground = (roll == 0 ? Blocks.STONE_BRICKS : roll == 1 ? Blocks.MOSSY_COBBLESTONE : Blocks.COBBLESTONE).defaultBlockState();
                } else {
                    ground = Blocks.GRASS_BLOCK.defaultBlockState();
                }
                b.set(x, 1, z, ground);
            }
        }
        b.set(c, 2, c, Blocks.CHISELED_QUARTZ_BLOCK);
        b.set(c, 3, c, cluster(Direction.UP));
        for (int[] spot : new int[][]{{c, c - 2}, {c, c + 2}, {c - 2, c}, {c + 2, c}, {c - 2, c - 2}, {c + 2, c + 2}, {c - 2, c + 2}, {c + 2, c - 2}}) {
            b.set(spot[0], 1, spot[1], Blocks.GRASS_BLOCK);
            b.set(spot[0], 2, spot[1], ModBlocks.MOONFLOWER.get());
        }
        for (int[] stone : new int[][]{{c, c - 4}, {c, c + 4}, {c - 4, c}, {c + 4, c}}) {
            b.set(stone[0], 1, stone[1], Blocks.STONE_BRICKS);
            b.fill(stone[0], 2, stone[1], stone[0], 4, stone[1], Blocks.CALCITE);
            b.set(stone[0], 5, stone[1], ModBlocks.MOON_LANTERN.get());
        }
        for (int[] stone : new int[][]{{c - 3, c - 3}, {c + 3, c - 3}, {c - 3, c + 3}, {c + 3, c + 3}}) {
            b.set(stone[0], 1, stone[1], Blocks.STONE_BRICKS);
            b.fill(stone[0], 2, stone[1], stone[0], 3, stone[1], Blocks.CALCITE);
            b.set(stone[0], 4, stone[1], cluster(Direction.UP));
        }
        b.decorateOpenGround(2, List.of(ModBlocks.GLOWCAP.get().defaultBlockState(), ModBlocks.BLUEBELL.get().defaultBlockState(),
                ModBlocks.MOONFLOWER.get().defaultBlockState(), ModBlocks.BLUE_FLOWER_CARPET.get().defaultBlockState(),
                ModBlocks.LAVENDER_FLOWER_CARPET.get().defaultBlockState(), ModBlocks.STARFLOWER.get().defaultBlockState()));
        for (int x = 0; x <= 10; x++) {
            for (int z = 0; z <= 10; z++) {
                if (b.isSet(x, 1, z)) {
                    b.clearAir(x, 2, z, x, 5, z);
                }
            }
        }
        return b;
    }

    // ------------------------------------------------------------------------------------------ mushroom cottage

    /** A fairy's cottage inside a giant spotted mushroom: bed, workbench, lantern, flower pots and a pantry chest. */
    private static Blueprint mushroomCottage() {
        Blueprint b = new Blueprint("mushroom_cottage", 9, 10, 9);
        b.fill(0, 0, 0, 8, 0, 8, Blocks.GRASS_BLOCK);
        b.fill(2, 0, 2, 6, 0, 6, ModBlocks.BLOSSOM.planks.get());
        b.set(4, 0, 8, Blocks.DIRT_PATH);
        b.set(4, 0, 7, ModBlocks.BLOSSOM.planks.get());

        BlockState stem = Blocks.MUSHROOM_STEM.defaultBlockState();
        for (int x = 1; x <= 7; x++) {
            for (int z = 1; z <= 7; z++) {
                if (x == 1 || x == 7 || z == 1 || z == 7) {
                    b.fill(x, 0, z, x, 4, z, stem);
                }
            }
        }
        BlockState door = ModBlocks.BLOSSOM.door.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT);
        b.set(4, 0, 7, ModBlocks.BLOSSOM.planks.get());
        b.set(4, 1, 7, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        b.set(4, 2, 7, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));

        BlockState paneNS = Blocks.PINK_STAINED_GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.NORTH, true).setValue(IronBarsBlock.SOUTH, true);
        BlockState paneEW = Blocks.PINK_STAINED_GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.EAST, true).setValue(IronBarsBlock.WEST, true);
        b.set(1, 2, 4, paneNS);
        b.set(7, 2, 4, paneNS);
        b.set(4, 2, 1, paneEW);
        b.set(2, 2, 7, paneEW);
        b.set(6, 2, 7, paneEW);

        // Furniture
        b.set(2, 1, 2, Blocks.PINK_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.HEAD));
        b.set(2, 1, 3, Blocks.PINK_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.FOOT));
        b.set(6, 1, 2, Blocks.CRAFTING_TABLE);
        b.chest(6, 1, 3, Direction.WEST, ModLootTables.MUSHROOM_COTTAGE);
        b.set(6, 1, 6, Blocks.BOOKSHELF);
        b.set(5, 1, 6, ModBlocks.POTTED.get(ModBlocks.ROSY_DAISY).get());
        b.set(2, 1, 6, ModBlocks.POTTED.get(ModBlocks.FAIRY_MUSHROOM).get());
        b.set(3, 1, 2, ModBlocks.POTTED.get(ModBlocks.BLUEBELL).get());
        b.fill(3, 1, 3, 5, 1, 5, Blocks.PINK_CARPET);
        b.set(4, 4, 4, ModBlocks.FAIRY_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, true));
        b.clearAir(2, 1, 2, 6, 4, 6);

        // Ceiling and the spotted cap
        b.fill(2, 5, 2, 6, 5, 6, ModBlocks.BLOSSOM.planks.get());
        BlockState cap = ModBlocks.FAIRY_MUSHROOM_BLOCK.get().defaultBlockState();
        int[] capLayers = {5, 6, 7, 8};
        double[] capRadii = {4.7, 4.0, 3.1, 1.9};
        for (int i = 0; i < capLayers.length; i++) {
            int y = capLayers[i];
            for (int x = 0; x <= 8; x++) {
                for (int z = 0; z <= 8; z++) {
                    double d = Math.sqrt((x - 4) * (x - 4) + (z - 4) * (z - 4));
                    if (d <= capRadii[i] && !b.isSet(x, y, z)) {
                        b.set(x, y, z, i == 0 ? cap.setValue(HugeMushroomBlock.DOWN, false) : cap);
                    }
                }
            }
        }

        // Garden around the cottage
        BlockState ripe = ModBlocks.STRAWBERRY_BUSH.get().defaultBlockState().setValue(StrawberryBushBlock.AGE, 3);
        b.set(0, 1, 3, ripe);
        b.set(0, 1, 4, ripe);
        b.set(8, 1, 5, ripe);
        b.set(0, 1, 1, ModBlocks.FAIRY_MUSHROOM.get());
        b.set(8, 1, 1, ModBlocks.GLOWCAP.get());
        b.set(8, 1, 7, ModBlocks.FAIRY_MUSHROOM.get());
        b.set(0, 1, 7, ModBlocks.CREAM_BUTTERCUP.get());
        b.set(1, 1, 8, ModBlocks.ROSY_DAISY.get());
        b.set(7, 1, 8, ModBlocks.MINT_POSY.get());
        b.set(3, 1, 8, ModBlocks.BLOSSOM.fence.get());
        b.set(3, 2, 8, ModBlocks.FAIRY_LANTERN.get());
        b.set(5, 1, 8, ModBlocks.CREAM_FLOWER_CARPET.get());
        b.decorateOpenGround(1, meadowDecor());
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                if (x == 0 || x == 8 || z == 0 || z == 8) {
                    b.clearAir(x, 1, z, x, 4, z);
                }
            }
        }
        return b;
    }

    // ------------------------------------------------------------------------------------------ helpers

    private static List<BlockState> meadowDecor() {
        return List.of(ModBlocks.ROSY_DAISY.get().defaultBlockState(), ModBlocks.CREAM_BUTTERCUP.get().defaultBlockState(),
                ModBlocks.STARFLOWER.get().defaultBlockState(), ModBlocks.MINT_POSY.get().defaultBlockState(),
                ModBlocks.PINK_FLOWER_CARPET.get().defaultBlockState(), ModBlocks.CREAM_FLOWER_CARPET.get().defaultBlockState(),
                ModBlocks.MINT_FLOWER_CARPET.get().defaultBlockState(), ModBlocks.BLUE_FLOWER_CARPET.get().defaultBlockState());
    }

    private static BlockState cluster(Direction facing) {
        return ModBlocks.FAIRY_CRYSTAL_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, facing);
    }

    private static BlockState petalState(Blueprint b, Block petals) {
        return petals.defaultBlockState()
                .setValue(PinkPetalsBlock.AMOUNT, 1 + b.random.nextInt(4))
                .setValue(PinkPetalsBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(net.minecraft.util.RandomSource.create(b.random.nextLong())));
    }
}
