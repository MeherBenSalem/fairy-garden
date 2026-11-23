package com.sugarplumstudios.fairygarden.datagen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.block.MoonflowerBlock;
import com.sugarplumstudios.fairygarden.block.StarbloomCropBlock;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.registry.ModItems;
import com.sugarplumstudios.fairygarden.registry.ModLootTables;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class ModLootProviders {
    private ModLootProviders() {
    }

    // ------------------------------------------------------------------------------------------------ blocks

    public static class Blocks extends BlockLootSubProvider {
        private static final float[] SAPLING_CHANCES = {0.05F, 0.0625F, 0.083333336F, 0.1F};

        public Blocks() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            for (WoodSet set : ModBlocks.WOOD_SETS) {
                for (Block block : new Block[]{set.log.get(), set.wood.get(), set.strippedLog.get(), set.strippedWood.get(), set.planks.get(),
                        set.stairs.get(), set.fence.get(), set.fenceGate.get(), set.trapdoor.get(), set.pressurePlate.get(),
                        set.button.get(), set.sign.get(), set.hangingSign.get()}) {
                    dropSelf(block);
                }
                add(set.slab.get(), createSlabItemTable(set.slab.get()));
                add(set.door.get(), createDoorTable(set.door.get()));
            }
            add(ModBlocks.BLOSSOM_LEAVES.get(), createLeavesDrops(ModBlocks.BLOSSOM_LEAVES.get(), ModBlocks.BLOSSOM_SAPLING.get(), SAPLING_CHANCES));
            add(ModBlocks.SNOW_BLOSSOM_LEAVES.get(), createLeavesDrops(ModBlocks.SNOW_BLOSSOM_LEAVES.get(), ModBlocks.SNOW_BLOSSOM_SAPLING.get(), SAPLING_CHANCES));
            add(ModBlocks.MOONWOOD_LEAVES.get(), createLeavesDrops(ModBlocks.MOONWOOD_LEAVES.get(), ModBlocks.MOONWOOD_SAPLING.get(), SAPLING_CHANCES));

            for (Block block : new Block[]{ModBlocks.BLOSSOM_SAPLING.get(), ModBlocks.SNOW_BLOSSOM_SAPLING.get(), ModBlocks.MOONWOOD_SAPLING.get(),
                    ModBlocks.FAIRY_MUSHROOM.get(), ModBlocks.GLOWCAP.get(), ModBlocks.MOONBELL.get(), ModBlocks.GLIMMER_LILY.get(),
                    ModBlocks.PINK_HYDRANGEA.get(), ModBlocks.BLUE_HYDRANGEA.get(), ModBlocks.LAVENDER_HYDRANGEA.get(),
                    ModBlocks.GIANT_FLOWER_STEM.get(), ModBlocks.GIANT_FLOWER_PISTIL.get(), ModBlocks.FAIRY_CRYSTAL_BLOCK.get(),
                    ModBlocks.FAIRY_LANTERN.get(), ModBlocks.MOON_LANTERN.get(), ModBlocks.LAVENDER_BALE.get()}) {
                dropSelf(block);
            }
            ModBlocks.GIANT_PETALS.forEach(petals -> dropSelf(petals.get()));
            ModBlocks.FLOWER_CARPETS.forEach(carpet -> dropSelf(carpet.get()));
            for (RegistryObject<? extends Block> flower : ModBlocks.SMALL_FLOWERS) {
                if (flower.get() != ModBlocks.MOONFLOWER.get()) {
                    dropSelf(flower.get());
                }
            }
            for (RegistryObject<? extends Block> flower : ModBlocks.TALL_FLOWERS) {
                add(flower.get(), createSinglePropConditionTable(flower.get(), DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
            }
            ModBlocks.POTTED.forEach((plant, pot) -> add(pot.get(), createPotFlowerItemTable(plant.get())));
            add(ModBlocks.BLOSSOM_PETALS.get(), createPetalsDrops(ModBlocks.BLOSSOM_PETALS.get()));
            add(ModBlocks.SNOW_BLOSSOM_PETALS.get(), createPetalsDrops(ModBlocks.SNOW_BLOSSOM_PETALS.get()));

            // Short grasses: shears keep the plant, otherwise a small chance of starbloom seeds.
            for (Block grass : new Block[]{ModBlocks.BLUSH_GRASS.get(), ModBlocks.MOONLIT_GRASS.get()}) {
                add(grass, createShearsDispatchTable(grass, applyExplosionDecay(grass,
                        LootItem.lootTableItem(ModItems.STARBLOOM_SEEDS.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.1F))
                                .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2)))));
            }

            // Moonflowers picked while open also give a moon petal.
            Block moonflower = ModBlocks.MOONFLOWER.get();
            add(moonflower, LootTable.lootTable()
                    .withPool(applyExplosionCondition(moonflower, LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(moonflower))))
                    .withPool(applyExplosionCondition(moonflower, LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .when(stateIs(moonflower, MoonflowerBlock.OPEN.getName(), "true"))
                            .add(LootItem.lootTableItem(ModItems.MOON_PETAL.get())))));

            // Starbloom: seeds always, fairy dust and extra seeds when ripe.
            Block starbloom = ModBlocks.STARBLOOM.get();
            LootItemCondition.Builder ripe = stateIs(starbloom, StarbloomCropBlock.AGE.getName(), String.valueOf(StarbloomCropBlock.MAX_AGE));
            add(starbloom, applyExplosionDecay(starbloom, LootTable.lootTable()
                    .withPool(LootPool.lootPool().add(LootItem.lootTableItem(ModItems.STARBLOOM_SEEDS.get())))
                    .withPool(LootPool.lootPool().when(ripe).add(LootItem.lootTableItem(ModItems.FAIRY_DUST.get())
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                            .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))))
                    .withPool(LootPool.lootPool().when(ripe).add(LootItem.lootTableItem(ModItems.STARBLOOM_SEEDS.get())
                            .apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 2))))));

            // Strawberry bush: like sweet berries.
            Block bush = ModBlocks.STRAWBERRY_BUSH.get();
            add(bush, applyExplosionDecay(bush, LootTable.lootTable()
                    .withPool(LootPool.lootPool().when(stateIs(bush, SweetBerryBushBlock.AGE.getName(), "3"))
                            .add(LootItem.lootTableItem(ModItems.STRAWBERRY.get()))
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F)))
                            .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)))
                    .withPool(LootPool.lootPool().when(stateIs(bush, SweetBerryBushBlock.AGE.getName(), "2"))
                            .add(LootItem.lootTableItem(ModItems.STRAWBERRY.get()))
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                            .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)))));

            add(ModBlocks.FAIRY_MUSHROOM_BLOCK.get(), createMushroomBlockDrop(ModBlocks.FAIRY_MUSHROOM_BLOCK.get(), ModBlocks.FAIRY_MUSHROOM.get()));
            add(ModBlocks.GLOWCAP_BLOCK.get(), createMushroomBlockDrop(ModBlocks.GLOWCAP_BLOCK.get(), ModBlocks.GLOWCAP.get()));
            add(ModBlocks.FAIRY_CRYSTAL_CLUSTER.get(), createSilkTouchDispatchTable(ModBlocks.FAIRY_CRYSTAL_CLUSTER.get(),
                    LootItem.lootTableItem(ModItems.CRYSTAL_SHARD.get())
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))
                            .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
        }

        private static LootItemCondition.Builder stateIs(Block block, String property, String value) {
            return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(
                            block.getStateDefinition().getProperty(property), value));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            Set<Block> known = new HashSet<>();
            ModBlocks.BLOCKS.getEntries().forEach(entry -> known.add(entry.get()));
            // Wall signs share the loot table of their standing sign.
            ModBlocks.WOOD_SETS.forEach(set -> {
                known.remove(set.wallSign.get());
                known.remove(set.wallHangingSign.get());
            });
            return known;
        }
    }

    // ------------------------------------------------------------------------------------------------ entities

    public static class Entities extends EntityLootSubProvider {
        public Entities() {
            super(FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        public void generate() {
            add(ModEntities.BUTTERFLY.get(), LootTable.lootTable());
            add(ModEntities.MOON_WISP.get(), LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.FAIRY_DUST.get())
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
                            .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))));
        }

        @Override
        protected Stream<EntityType<?>> getKnownEntityTypes() {
            return ModEntities.ENTITIES.getEntries().stream().map(RegistryObject::get);
        }
    }

    // ------------------------------------------------------------------------------------------------ chests

    public static class Chests implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceLocation, LootTable.Builder> out) {
            out.accept(ModLootTables.FLOWER_SHRINE, LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(3, 5))
                            .add(item(ModItems.FAIRY_DUST.get(), 10, 1, 3))
                            .add(item(ModItems.STRAWBERRY.get(), 10, 2, 5))
                            .add(item(ModItems.LAVENDER_TEA.get(), 6, 1, 2))
                            .add(item(ModItems.STARBLOOM_SEEDS.get(), 8, 1, 3))
                            .add(item(ModItems.MOON_PETAL.get(), 5, 1, 2))
                            .add(item(ModBlocks.BLOSSOM_SAPLING.get(), 5, 1, 2))
                            .add(item(ModBlocks.SNOW_BLOSSOM_SAPLING.get(), 4, 1, 1))
                            .add(item(ModItems.CRYSTAL_SHARD.get(), 6, 1, 3))
                            .add(item(Items.EMERALD, 4, 1, 3))
                            .add(item(Items.GOLD_NUGGET, 6, 2, 6)))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(item(ModItems.FLOWER_CROWN.get(), 3, 1, 1))
                            .add(item(Items.GOLDEN_APPLE, 1, 1, 1))
                            .add(LootItem.lootTableItem(Items.BOOK).setWeight(2).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
                            .add(EmptyLootItem.emptyItem().setWeight(4))));

            out.accept(ModLootTables.HIDDEN_FAIRY_GARDEN, LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(4, 7))
                            .add(item(ModItems.FAIRY_DUST.get(), 12, 2, 5))
                            .add(item(ModItems.CRYSTAL_SHARD.get(), 8, 2, 4))
                            .add(item(ModItems.STARBLOOM_SEEDS.get(), 8, 2, 4))
                            .add(item(ModItems.MOON_PETAL.get(), 6, 1, 3))
                            .add(item(ModBlocks.FAIRY_LANTERN.get(), 4, 1, 2))
                            .add(item(ModBlocks.GLIMMER_LILY.get(), 4, 1, 3))
                            .add(item(ModBlocks.CRYSTAL_FLOWER.get(), 5, 1, 3))
                            .add(item(ModBlocks.FAIRY_FOXGLOVE.get(), 4, 1, 2))
                            .add(item(Items.EMERALD, 6, 2, 5))
                            .add(item(Items.GOLD_INGOT, 5, 1, 3))
                            .add(item(Items.AMETHYST_SHARD, 5, 2, 5)))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(item(ModItems.FLOWER_CROWN.get(), 4, 1, 1))
                            .add(item(Items.DIAMOND, 3, 1, 2))
                            .add(item(Items.ENCHANTED_GOLDEN_APPLE, 1, 1, 1))
                            .add(LootItem.lootTableItem(Items.BOOK).setWeight(4).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
                            .add(EmptyLootItem.emptyItem().setWeight(2))));

            out.accept(ModLootTables.MOONSTONE_CIRCLE, LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(3, 6))
                            .add(item(ModItems.MOON_PETAL.get(), 10, 2, 4))
                            .add(item(ModItems.FAIRY_DUST.get(), 8, 1, 3))
                            .add(item(ModBlocks.MOONWOOD_SAPLING.get(), 6, 1, 2))
                            .add(item(ModBlocks.MOONFLOWER.get(), 6, 1, 3))
                            .add(item(ModBlocks.GLOWCAP.get(), 5, 1, 3))
                            .add(item(ModBlocks.MOON_LANTERN.get(), 3, 1, 1))
                            .add(item(ModItems.CRYSTAL_SHARD.get(), 6, 1, 3))
                            .add(item(Items.LAPIS_LAZULI, 6, 3, 8))
                            .add(item(Items.ENDER_PEARL, 3, 1, 2))
                            .add(item(Items.AMETHYST_SHARD, 5, 2, 4)))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(Items.BOOK).setWeight(4).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
                            .add(item(Items.DIAMOND, 2, 1, 1))
                            .add(item(Items.GOLDEN_APPLE, 2, 1, 1))
                            .add(EmptyLootItem.emptyItem().setWeight(3))));

            out.accept(ModLootTables.MUSHROOM_COTTAGE, LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(3, 6))
                            .add(item(ModItems.STRAWBERRY_SHORTCAKE.get(), 8, 1, 2))
                            .add(item(ModItems.STRAWBERRY.get(), 10, 2, 6))
                            .add(item(ModItems.LAVENDER_TEA.get(), 6, 1, 2))
                            .add(item(Items.BREAD, 6, 1, 3))
                            .add(item(ModBlocks.FAIRY_MUSHROOM.get(), 6, 1, 3))
                            .add(item(ModBlocks.GLOWCAP.get(), 4, 1, 2))
                            .add(item(ModItems.STARBLOOM_SEEDS.get(), 6, 1, 3))
                            .add(item(ModItems.FAIRY_DUST.get(), 5, 1, 2))
                            .add(item(Items.STRING, 5, 2, 5))
                            .add(item(Items.PINK_WOOL, 4, 1, 3))
                            .add(item(Items.BOOK, 4, 1, 2)))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(item(ModItems.FLOWER_CROWN.get(), 3, 1, 1))
                            .add(item(Items.EMERALD, 3, 1, 3))
                            .add(EmptyLootItem.emptyItem().setWeight(4))));

            out.accept(ModLootTables.FAIRY_CACHE, LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(2, 4))
                            .add(item(ModItems.FAIRY_DUST.get(), 10, 2, 5))
                            .add(item(ModItems.CRYSTAL_SHARD.get(), 8, 2, 4))
                            .add(item(ModItems.MOON_PETAL.get(), 5, 1, 3))
                            .add(item(Items.GOLD_INGOT, 5, 1, 3))
                            .add(item(Items.EMERALD, 6, 2, 5))
                            .add(item(Items.DIAMOND, 2, 1, 1))
                            .add(item(ModItems.FLOWER_CROWN.get(), 2, 1, 1))
                            .add(LootItem.lootTableItem(Items.BOOK).setWeight(2).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))));
        }

        private static LootItem.Builder<?> item(ItemLike item, int weight, int min, int max) {
            LootItem.Builder<?> builder = LootItem.lootTableItem(item).setWeight(weight);
            if (max > 1) {
                builder.apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
            }
            return builder;
        }
    }
}
