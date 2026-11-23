package com.sugarplumstudios.fairygarden.gametest;

import com.mojang.authlib.GameProfile;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.block.MoonflowerBlock;
import com.sugarplumstudios.fairygarden.block.StarbloomCropBlock;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.registry.ModItems;
import com.sugarplumstudios.fairygarden.registry.ModLootTables;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import com.sugarplumstudios.fairygarden.worldgen.ModBiomes;
import com.sugarplumstudios.fairygarden.worldgen.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.UUID;

/**
 * Automated in-game checks, run headlessly with {@code ./gradlew runGameTestServer}. They use the mod's plants,
 * items and blocks on the server the way a player would and check what happens.
 */
@GameTestHolder(FairyGarden.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FairyGameTests {
    private static final String TEMPLATE = "empty";
    private static final BlockPos GROUND = new BlockPos(1, 0, 1);
    private static final BlockPos PLANT = new BlockPos(1, 1, 1);

    private FairyGameTests() {
    }

    // ------------------------------------------------------------------ helpers

    private static ServerPlayer player(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(("fairygarden-" + name).getBytes()), "fg_" + name));
    }

    /** Uses {@code stack} on the top face of the block at {@code relative}, the way a player right-clicks with it. */
    private static InteractionResult useOn(GameTestHelper helper, ServerPlayer player, ItemStack stack, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos).relative(Direction.UP, 0.5D), Direction.UP, pos, false);
        InteractionResult result = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return result;
    }

    private static BlockState at(GameTestHelper helper, BlockPos relative) {
        return helper.getBlockState(relative);
    }

    private static List<ItemStack> drops(GameTestHelper helper, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        return Block.getDrops(helper.getLevel().getBlockState(pos), helper.getLevel(), pos, null);
    }

    private static boolean contains(List<ItemStack> stacks, Item item) {
        return stacks.stream().anyMatch(stack -> stack.is(item));
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    // ------------------------------------------------------------------ magical plants

    @GameTest(template = TEMPLATE)
    public static void fairyDustRipensStarbloomAndItDropsFairyDust(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.FARMLAND);
        helper.setBlock(PLANT, ModBlocks.STARBLOOM.get().defaultBlockState());
        ItemStack dust = new ItemStack(ModItems.FAIRY_DUST.get(), 2);
        InteractionResult result = useOn(helper, player(helper, "starbloom"), dust, PLANT);
        check(helper, result.consumesAction(), "fairy dust did nothing to a starbloom sprout");
        check(helper, at(helper, PLANT).getValue(StarbloomCropBlock.AGE) == StarbloomCropBlock.MAX_AGE, "fairy dust didn't ripen the starbloom");
        check(helper, dust.getCount() == 1, "fairy dust wasn't used up");
        check(helper, contains(drops(helper, PLANT), ModItems.FAIRY_DUST.get()), "a ripe starbloom didn't drop fairy dust");
        check(helper, contains(drops(helper, PLANT), ModItems.STARBLOOM_SEEDS.get()), "a ripe starbloom didn't drop seeds");
        helper.setBlock(PLANT, ModBlocks.STARBLOOM.get().defaultBlockState());
        check(helper, !contains(drops(helper, PLANT), ModItems.FAIRY_DUST.get()), "an unripe starbloom dropped fairy dust");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fairyDustOpensMoonflowerAndOpenOnesGiveMoonPetals(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.GRASS_BLOCK);
        helper.setBlock(PLANT, ModBlocks.MOONFLOWER.get().defaultBlockState().setValue(MoonflowerBlock.OPEN, false));
        check(helper, !contains(drops(helper, PLANT), ModItems.MOON_PETAL.get()), "a closed moonflower dropped a moon petal");
        useOn(helper, player(helper, "moonflower"), new ItemStack(ModItems.FAIRY_DUST.get()), PLANT);
        check(helper, at(helper, PLANT).getValue(MoonflowerBlock.OPEN), "fairy dust didn't open the moonflower");
        List<ItemStack> drops = drops(helper, PLANT);
        check(helper, contains(drops, ModItems.MOON_PETAL.get()), "an open moonflower didn't drop a moon petal");
        check(helper, contains(drops, ModBlocks.MOONFLOWER.get().asItem()), "an open moonflower didn't drop itself");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fairyDustGivesThreeRoundsOfGrowth(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.GRASS_BLOCK);
        helper.setBlock(PLANT, ModBlocks.STRAWBERRY_BUSH.get().defaultBlockState().setValue(SweetBerryBushBlock.AGE, 0));
        useOn(helper, player(helper, "growth"), new ItemStack(ModItems.FAIRY_DUST.get()), PLANT);
        check(helper, at(helper, PLANT).getValue(SweetBerryBushBlock.AGE) == 3, "fairy dust didn't grow the strawberry bush three stages");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void strawberryBushIsPickedAndRegrows(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.GRASS_BLOCK);
        helper.setBlock(PLANT, ModBlocks.STRAWBERRY_BUSH.get().defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3));
        ServerPlayer player = player(helper, "strawberry");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        BlockPos pos = helper.absolutePos(PLANT);
        InteractionResult result = helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        check(helper, result.consumesAction(), "picking a fruiting strawberry bush did nothing");
        check(helper, at(helper, PLANT).getValue(SweetBerryBushBlock.AGE) == 1, "the picked bush didn't go back to stage 1");
        helper.assertItemEntityPresent(ModItems.STRAWBERRY.get(), PLANT, 2.0D);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "daynight", timeoutTicks = 400)
    public static void moonflowersFollowTheTimeAndStarbloomGrowsOnlyAtNight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        long before = level.getDayTime();
        // The game test server builds its test plots inside a normally generated world, under the terrain, so the
        // plot's starbloom is covered: it must not grow even at night. A second starbloom at the build limit, above
        // everything, stands under the open sky.
        helper.setBlock(GROUND, Blocks.FARMLAND);
        helper.setBlock(PLANT, ModBlocks.STARBLOOM.get().defaultBlockState());
        BlockPos covered = helper.absolutePos(PLANT);
        BlockPos open = new BlockPos(covered.getX(), level.getMaxBuildHeight() - 2, covered.getZ());
        level.setBlockAndUpdate(open.below(), Blocks.FARMLAND.defaultBlockState());
        level.setBlockAndUpdate(open, ModBlocks.STARBLOOM.get().defaultBlockState());
        BlockPos flowerGround = new BlockPos(3, 0, 1);
        BlockPos flower = new BlockPos(3, 1, 1);
        BlockPos pos = helper.absolutePos(flower);
        helper.setBlock(flowerGround, Blocks.GRASS_BLOCK);
        helper.setBlock(flower, ModBlocks.MOONFLOWER.get().defaultBlockState().setValue(MoonflowerBlock.OPEN, false));
        level.setDayTime(18000L);
        // The sky darkens on the next server tick and the light engine needs a moment for the new blocks,
        // so each step waits until its condition holds (or the test times out).
        helper.startSequence()
                .thenWaitUntil(() -> check(helper, !level.isDay(), "it isn't night at midnight"))
                .thenWaitUntil(() -> check(helper, level.canSeeSky(open), "the starbloom at the build limit can't see the sky (sky light "
                        + level.getBrightness(net.minecraft.world.level.LightLayer.SKY, open) + ")"))
                .thenExecute(() -> {
                    check(helper, StarbloomCropBlock.canGrowNow(level, open), "starbloom can't grow at midnight under the open sky");
                    check(helper, !StarbloomCropBlock.canGrowNow(level, covered), "starbloom can grow underground");
                    level.getBlockState(pos).randomTick(level, pos, level.random);
                    check(helper, at(helper, flower).getValue(MoonflowerBlock.OPEN), "the moonflower didn't open at night");
                    level.setDayTime(6000L);
                })
                .thenWaitUntil(() -> check(helper, level.isDay(), "it isn't day at noon"))
                .thenExecute(() -> {
                    check(helper, !StarbloomCropBlock.canGrowNow(level, open), "starbloom can grow at noon");
                    level.getBlockState(pos).randomTick(level, pos, level.random);
                    check(helper, !at(helper, flower).getValue(MoonflowerBlock.OPEN), "the moonflower didn't close by day");
                    level.setBlockAndUpdate(open, Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(open.below(), Blocks.AIR.defaultBlockState());
                    level.setDayTime(before);
                })
                .thenSucceed();
    }

    // ------------------------------------------------------------------ blocks and items

    @GameTest(template = TEMPLATE)
    public static void giantPetalsSoftenFalls(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBlock(GROUND, ModBlocks.PINK_GIANT_PETALS.get());
        BlockPos stone = new BlockPos(3, 0, 1);
        helper.setBlock(stone, Blocks.STONE);
        Cow soft = helper.spawn(EntityType.COW, PLANT);
        Cow hard = helper.spawn(EntityType.COW, new BlockPos(3, 1, 1));
        BlockPos petals = helper.absolutePos(GROUND);
        level.getBlockState(petals).getBlock().fallOn(level, level.getBlockState(petals), petals, soft, 20.0F);
        BlockPos ground = helper.absolutePos(stone);
        level.getBlockState(ground).getBlock().fallOn(level, level.getBlockState(ground), ground, hard, 20.0F);
        // A 20-block fall deals 17 damage; giant petals take it down to a fifth, rounded up: 4.
        check(helper, Math.abs(soft.getHealth() - (soft.getMaxHealth() - 4.0F)) < 0.01F,
                "a 20-block fall onto giant petals left the cow at " + soft.getHealth() + " health");
        check(helper, hard.isDeadOrDying(), "the control fall onto stone didn't kill the cow");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void axesStripBothWoods(GameTestHelper helper) {
        ServerPlayer player = player(helper, "axe");
        int x = 1;
        for (WoodSet wood : ModBlocks.WOOD_SETS) {
            BlockPos log = new BlockPos(x, 1, 1);
            BlockPos block = new BlockPos(x, 1, 3);
            helper.setBlock(log, wood.log.get());
            helper.setBlock(block, wood.wood.get());
            useOn(helper, player, new ItemStack(Items.IRON_AXE), log);
            useOn(helper, player, new ItemStack(Items.IRON_AXE), block);
            check(helper, at(helper, log).is(wood.strippedLog.get()), wood.name + " log wasn't stripped");
            check(helper, at(helper, block).is(wood.strippedWood.get()), wood.name + " wood wasn't stripped");
            check(helper, at(helper, log).getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y, wood.name + " log lost its axis");
            check(helper, at(helper, log).is(BlockTags.LOGS_THAT_BURN), wood.name + " stripped log isn't in logs_that_burn");
            check(helper, wood.planks.get().defaultBlockState().is(BlockTags.PLANKS), wood.name + " planks aren't in minecraft:planks");
            x += 2;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void lavenderTeaCuresPoisonAndReturnsTheBottle(GameTestHelper helper) {
        ServerPlayer player = player(helper, "tea");
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
        ItemStack tea = new ItemStack(ModItems.LAVENDER_TEA.get());
        ItemStack left = tea.finishUsingItem(helper.getLevel(), player);
        check(helper, left.is(Items.GLASS_BOTTLE), "drinking the last lavender tea left " + left + " instead of a glass bottle");
        check(helper, !player.hasEffect(MobEffects.POISON), "lavender tea didn't cure poison");
        check(helper, !player.hasEffect(MobEffects.CONFUSION), "lavender tea didn't cure nausea");
        check(helper, player.hasEffect(MobEffects.REGENERATION), "lavender tea didn't give regeneration");
        player.removeAllEffects();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void lanternsAndCrystalsGlow(GameTestHelper helper) {
        check(helper, ModBlocks.FAIRY_LANTERN.get().defaultBlockState().getLightEmission() == 15, "fairy lantern isn't light 15");
        check(helper, ModBlocks.MOON_LANTERN.get().defaultBlockState().getLightEmission() == 14, "moon lantern isn't light 14");
        check(helper, ModBlocks.FAIRY_CRYSTAL_BLOCK.get().defaultBlockState().getLightEmission() > 0, "fairy crystal block doesn't glow");
        check(helper, ModBlocks.FAIRY_CRYSTAL_CLUSTER.get().defaultBlockState().getLightEmission() > 0, "fairy crystal cluster doesn't glow");
        check(helper, ModItems.FLOWER_CROWN.get().getType() == ArmorItem.Type.HELMET, "the flower crown isn't a helmet");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void creaturesSpawnAndTick(GameTestHelper helper) {
        var butterfly = helper.spawn(ModEntities.BUTTERFLY.get(), new BlockPos(1, 2, 1));
        var wisp = helper.spawn(ModEntities.MOON_WISP.get(), new BlockPos(3, 2, 3));
        check(helper, butterfly != null && wisp != null, "a creature failed to spawn");
        helper.runAfterDelay(40, () -> {
            check(helper, butterfly.isAlive(), "the butterfly died within two seconds");
            helper.succeed();
        });
    }

    // ------------------------------------------------------------------ data

    @GameTest(template = TEMPLATE)
    public static void biomesAndStructuresAreRegistered(GameTestHelper helper) {
        var access = helper.getLevel().registryAccess();
        var biomes = access.registryOrThrow(Registries.BIOME);
        for (var biome : ModBiomes.ALL) {
            check(helper, biomes.containsKey(biome), "biome " + biome.location() + " isn't registered");
        }
        var structures = access.registryOrThrow(Registries.STRUCTURE);
        for (var structure : ModStructures.ALL) {
            check(helper, structures.containsKey(structure), "structure " + structure.location() + " isn't registered");
        }
        var sets = access.registryOrThrow(Registries.STRUCTURE_SET);
        for (ResourceKey<?> set : List.of(ModStructures.FLOWER_SHRINE_SET, ModStructures.HIDDEN_FAIRY_GARDEN_SET,
                ModStructures.MOONSTONE_CIRCLE_SET, ModStructures.MUSHROOM_COTTAGE_SET)) {
            check(helper, sets.containsKey(set.location()), "structure set " + set.location() + " isn't registered");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void everyBlockAndChestHasALootTable(GameTestHelper helper) {
        var loot = helper.getLevel().getServer().getLootData();
        int checked = 0;
        for (RegistryObject<Block> entry : ModBlocks.BLOCKS.getEntries()) {
            ResourceLocation id = entry.get().getLootTable();
            if (id.equals(BuiltInLootTables.EMPTY)) {
                continue;
            }
            check(helper, loot.getLootTable(id) != LootTable.EMPTY, "no loot table " + id + " for " + entry.getId());
            checked++;
        }
        check(helper, checked >= 85, "only " + checked + " blocks have loot tables");
        for (ResourceLocation chest : List.of(ModLootTables.FLOWER_SHRINE, ModLootTables.HIDDEN_FAIRY_GARDEN, ModLootTables.MOONSTONE_CIRCLE,
                ModLootTables.MUSHROOM_COTTAGE, ModLootTables.FAIRY_CACHE)) {
            check(helper, loot.getLootTable(chest) != LootTable.EMPTY, "no chest loot table " + chest);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void craftingRecipesAreLoaded(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<CraftingRecipe> recipes = level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);
        for (Item wanted : List.of(ModBlocks.FAIRY_LANTERN.get().asItem(), ModBlocks.MOON_LANTERN.get().asItem(), ModItems.FLOWER_CROWN.get(),
                ModItems.LAVENDER_TEA.get(), ModItems.STRAWBERRY_SHORTCAKE.get(), ModItems.FAIRY_DUST.get(),
                ModBlocks.GLIMMER_LILY.get().asItem(), ModBlocks.LAVENDER_BALE.get().asItem(), ModBlocks.PINK_FLOWER_CARPET.get().asItem(),
                ModBlocks.MOONWOOD.planks.get().asItem(), ModBlocks.BLOSSOM.chestBoat.get())) {
            boolean found = recipes.stream().anyMatch(recipe -> recipe.getResultItem(level.registryAccess()).is(wanted));
            check(helper, found, "no crafting recipe makes " + wanted);
        }
        helper.succeed();
    }
}
