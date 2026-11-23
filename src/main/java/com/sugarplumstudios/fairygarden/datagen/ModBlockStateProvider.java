package com.sugarplumstudios.fairygarden.datagen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.block.MoonflowerBlock;
import com.sugarplumstudios.fairygarden.block.StarbloomCropBlock;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper files) {
        super(output, FairyGarden.MOD_ID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        woodSet(ModBlocks.BLOSSOM, false);
        woodSet(ModBlocks.MOONWOOD, true);

        leaves(ModBlocks.BLOSSOM_LEAVES.get(), false);
        leaves(ModBlocks.SNOW_BLOSSOM_LEAVES.get(), false);
        leaves(ModBlocks.MOONWOOD_LEAVES.get(), true);
        cross(ModBlocks.BLOSSOM_SAPLING.get(), false);
        cross(ModBlocks.SNOW_BLOSSOM_SAPLING.get(), false);
        cross(ModBlocks.MOONWOOD_SAPLING.get(), true);

        for (RegistryObject<? extends Block> flower : ModBlocks.SMALL_FLOWERS) {
            Block block = flower.get();
            if (block instanceof MoonflowerBlock) {
                ModelFile closed = crossModel(name(block), blockTex(name(block)), false);
                ModelFile open = crossModel(name(block) + "_open", blockTex(name(block) + "_open"), true);
                getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(MoonflowerBlock.OPEN) ? open : closed).build());
            } else {
                cross(block, block == ModBlocks.CRYSTAL_FLOWER.get());
            }
        }
        for (RegistryObject<? extends Block> flower : ModBlocks.TALL_FLOWERS) {
            tallPlant(flower.get());
        }
        cross(ModBlocks.BLUSH_GRASS.get(), false);
        cross(ModBlocks.MOONLIT_GRASS.get(), false);
        cross(ModBlocks.FAIRY_MUSHROOM.get(), false);
        cross(ModBlocks.GLOWCAP.get(), true);
        cross(ModBlocks.MOONBELL.get(), true);

        for (Block bush : new Block[]{ModBlocks.PINK_HYDRANGEA.get(), ModBlocks.BLUE_HYDRANGEA.get(), ModBlocks.LAVENDER_HYDRANGEA.get()}) {
            simpleBlock(bush, models().withExistingParent(name(bush), modLoc("block/flowering_bush"))
                    .texture("side", blockTex(name(bush))).texture("top", blockTex(name(bush) + "_top")).renderType("cutout"));
        }

        Block strawberries = ModBlocks.STRAWBERRY_BUSH.get();
        getVariantBuilder(strawberries).forAllStates(state -> {
            int age = state.getValue(SweetBerryBushBlock.AGE);
            return ConfiguredModel.builder().modelFile(models().cross(name(strawberries) + "_stage" + age,
                    blockTex(name(strawberries) + "_stage" + age)).renderType("cutout")).build();
        });
        Block starbloom = ModBlocks.STARBLOOM.get();
        getVariantBuilder(starbloom).forAllStates(state -> {
            int age = state.getValue(StarbloomCropBlock.AGE);
            String model = name(starbloom) + "_stage" + age;
            ModelFile file = age == StarbloomCropBlock.MAX_AGE
                    ? models().withExistingParent(model, modLoc("block/emissive_crop")).texture("crop", blockTex(model)).renderType("cutout")
                    : models().crop(model, blockTex(model)).renderType("cutout");
            return ConfiguredModel.builder().modelFile(file).build();
        });

        mushroomBlock(ModBlocks.FAIRY_MUSHROOM_BLOCK.get(), false);
        mushroomBlock(ModBlocks.GLOWCAP_BLOCK.get(), true);

        Block lily = ModBlocks.GLIMMER_LILY.get();
        ModelFile lilyModel = models().withExistingParent(name(lily), modLoc("block/template_glimmer_lily"))
                .texture("pad", blockTex(name(lily))).texture("flower", blockTex(name(lily) + "_flower")).renderType("cutout");
        getVariantBuilder(lily).forAllStates(state -> ConfiguredModel.allYRotations(lilyModel, 0, false));

        petals(ModBlocks.BLOSSOM_PETALS.get());
        petals(ModBlocks.SNOW_BLOSSOM_PETALS.get());
        for (RegistryObject<? extends Block> carpet : ModBlocks.FLOWER_CARPETS) {
            simpleBlock(carpet.get(), models().withExistingParent(name(carpet.get()), modLoc("block/flower_carpet"))
                    .texture("carpet", blockTex(name(carpet.get()))).renderType("cutout"));
        }

        axisBlock(ModBlocks.GIANT_FLOWER_STEM.get(), blockTex("giant_flower_stem"), blockTex("giant_flower_stem_top"));
        for (RegistryObject<? extends Block> petals : ModBlocks.GIANT_PETALS) {
            simpleBlock(petals.get());
        }
        simpleBlock(ModBlocks.GIANT_FLOWER_PISTIL.get(), models().withExistingParent(name(ModBlocks.GIANT_FLOWER_PISTIL.get()),
                modLoc("block/emissive_cube_all")).texture("all", blockTex("giant_flower_pistil")));
        simpleBlock(ModBlocks.FAIRY_CRYSTAL_BLOCK.get(), models().withExistingParent(name(ModBlocks.FAIRY_CRYSTAL_BLOCK.get()),
                modLoc("block/emissive_cube_all")).texture("all", blockTex("fairy_crystal_block")));
        Block cluster = ModBlocks.FAIRY_CRYSTAL_CLUSTER.get();
        ModelFile clusterModel = crossModel(name(cluster), blockTex(name(cluster)), true);
        getVariantBuilder(cluster).forAllStates(state -> {
            Direction facing = state.getValue(AmethystClusterBlock.FACING);
            int x = switch (facing) {
                case DOWN -> 180;
                case UP -> 0;
                default -> 90;
            };
            int y = switch (facing) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(clusterModel).rotationX(x).rotationY(y).build();
        });

        lantern(ModBlocks.FAIRY_LANTERN.get());
        lantern(ModBlocks.MOON_LANTERN.get());
        axisBlock(ModBlocks.LAVENDER_BALE.get(), blockTex("lavender_bale_side"), blockTex("lavender_bale_top"));

        for (Map.Entry<RegistryObject<? extends Block>, RegistryObject<FlowerPotBlock>> entry : ModBlocks.POTTED.entrySet()) {
            Block plant = entry.getKey().get();
            String texture = plant == ModBlocks.MOONFLOWER.get() ? name(plant) + "_open" : name(plant);
            Block pot = entry.getValue().get();
            simpleBlock(pot, models().withExistingParent(name(pot), mcLoc("block/flower_pot_cross"))
                    .texture("plant", blockTex(texture)).renderType("cutout"));
        }
    }

    // --------------------------------------------------------------------------------------------- woods

    private void woodSet(WoodSet set, boolean glowing) {
        String n = set.name;
        if (glowing) {
            // Moonwood bark has glowing veins rendered at full brightness.
            glowingLog(set.log.get(), blockTex(n + "_log"), blockTex(n + "_log_top"), blockTex(n + "_log_glow"), blockTex(n + "_log_top_glow"));
            glowingLog(set.wood.get(), blockTex(n + "_log"), blockTex(n + "_log"), blockTex(n + "_log_glow"), blockTex(n + "_log_glow"));
        } else {
            logBlock(set.log.get());
            axisBlock(set.wood.get(), blockTex(n + "_log"), blockTex(n + "_log"));
        }
        axisBlock(set.strippedLog.get(), blockTex("stripped_" + n + "_log"), blockTex("stripped_" + n + "_log_top"));
        axisBlock(set.strippedWood.get(), blockTex("stripped_" + n + "_log"), blockTex("stripped_" + n + "_log"));

        ResourceLocation planks = blockTex(n + "_planks");
        simpleBlock(set.planks.get());
        stairsBlock(set.stairs.get(), planks);
        slabBlock(set.slab.get(), planks, planks);
        fenceBlock(set.fence.get(), planks);
        fenceGateBlock(set.fenceGate.get(), planks);
        doorBlockWithRenderType(set.door.get(), blockTex(n + "_door_bottom"), blockTex(n + "_door_top"), "cutout");
        trapdoorBlockWithRenderType(set.trapdoor.get(), blockTex(n + "_trapdoor"), true, "cutout");
        pressurePlateBlock(set.pressurePlate.get(), planks);
        buttonBlock(set.button.get(), planks);
        signBlock(set.sign.get(), set.wallSign.get(), planks);
        ModelFile hanging = models().sign(n + "_hanging_sign", blockTex("stripped_" + n + "_log"));
        simpleBlock(set.hangingSign.get(), hanging);
        simpleBlock(set.wallHangingSign.get(), hanging);

        // Inventory models used by the item model provider.
        models().fenceInventory(n + "_fence_inventory", planks);
        models().buttonInventory(n + "_button_inventory", planks);
    }

    private void glowingLog(net.minecraft.world.level.block.RotatedPillarBlock block, ResourceLocation side, ResourceLocation end,
                            ResourceLocation glow, ResourceLocation glowEnd) {
        ModelFile vertical = models().withExistingParent(name(block), modLoc("block/glowing_cube_column"))
                .texture("side", side).texture("end", end).texture("overlay", glow).texture("overlay_end", glowEnd).renderType("cutout");
        ModelFile horizontal = models().withExistingParent(name(block) + "_horizontal", modLoc("block/glowing_cube_column_horizontal"))
                .texture("side", side).texture("end", end).texture("overlay", glow).texture("overlay_end", glowEnd).renderType("cutout");
        axisBlock(block, vertical, horizontal);
    }

    // --------------------------------------------------------------------------------------------- plants

    private void leaves(Block block, boolean glowing) {
        ModelFile model = glowing
                ? models().withExistingParent(name(block), modLoc("block/glowing_leaves")).texture("all", blockTex(name(block)))
                .texture("overlay", blockTex(name(block) + "_glow")).renderType("cutout_mipped")
                : models().cubeAll(name(block), blockTex(name(block))).renderType("cutout_mipped");
        simpleBlock(block, model);
    }

    private ModelFile crossModel(String name, ResourceLocation texture, boolean emissive) {
        return emissive
                ? models().withExistingParent(name, modLoc("block/emissive_cross")).texture("cross", texture).renderType("cutout")
                : models().cross(name, texture).renderType("cutout");
    }

    private void cross(Block block, boolean emissive) {
        simpleBlock(block, crossModel(name(block), blockTex(name(block)), emissive));
    }

    private void tallPlant(Block block) {
        ModelFile bottom = models().cross(name(block) + "_bottom", blockTex(name(block) + "_bottom")).renderType("cutout");
        ModelFile top = models().cross(name(block) + "_top", blockTex(name(block) + "_top")).renderType("cutout");
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER ? bottom : top).build());
    }

    private void mushroomBlock(Block block, boolean emissive) {
        ModelFile outside = models().withExistingParent(name(block),
                        emissive ? modLoc("block/emissive_single_face") : mcLoc("block/template_single_face"))
                .texture("texture", blockTex(name(block)));
        ModelFile inside = models().getExistingFile(mcLoc("block/mushroom_block_inside"));
        models().cubeAll(name(block) + "_inventory", blockTex(name(block)));
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        Object[][] faces = {
                {HugeMushroomBlock.NORTH, 0, 0}, {HugeMushroomBlock.EAST, 0, 90}, {HugeMushroomBlock.SOUTH, 0, 180},
                {HugeMushroomBlock.WEST, 0, 270}, {HugeMushroomBlock.UP, 270, 0}, {HugeMushroomBlock.DOWN, 90, 0}};
        for (Object[] face : faces) {
            BooleanProperty property = (BooleanProperty) face[0];
            int x = (Integer) face[1];
            int y = (Integer) face[2];
            builder.part().modelFile(outside).rotationX(x).rotationY(y).uvLock(true).addModel().condition(property, true).end();
            builder.part().modelFile(inside).rotationX(x).rotationY(y).uvLock(true).addModel().condition(property, false).end();
        }
    }

    private void petals(Block block) {
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        ModelFile[] stages = new ModelFile[4];
        for (int i = 0; i < 4; i++) {
            stages[i] = models().withExistingParent(name(block) + "_" + (i + 1), mcLoc("block/flowerbed_" + (i + 1)))
                    .texture("flowerbed", blockTex(name(block))).texture("stem", blockTex(name(block) + "_stem")).renderType("cutout");
        }
        Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int f = 0; f < facings.length; f++) {
            for (int stage = 1; stage <= 4; stage++) {
                Integer[] amounts = new Integer[5 - stage];
                for (int a = stage; a <= 4; a++) {
                    amounts[a - stage] = a;
                }
                builder.part().modelFile(stages[stage - 1]).rotationY(f * 90).addModel()
                        .condition(PinkPetalsBlock.FACING, facings[f])
                        .condition(PinkPetalsBlock.AMOUNT, amounts).end();
            }
        }
    }

    private void lantern(Block block) {
        ModelFile standing = models().withExistingParent(name(block), mcLoc("block/template_lantern"))
                .texture("lantern", blockTex(name(block))).renderType("cutout");
        ModelFile hanging = models().withExistingParent(name(block) + "_hanging", mcLoc("block/template_hanging_lantern"))
                .texture("lantern", blockTex(name(block))).renderType("cutout");
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(LanternBlock.HANGING) ? hanging : standing).build());
    }

    // --------------------------------------------------------------------------------------------- helpers

    private static String name(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block).getPath();
    }

    private ResourceLocation blockTex(String name) {
        return modLoc("block/" + name);
    }
}
