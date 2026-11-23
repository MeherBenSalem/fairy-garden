package com.sugarplumstudios.fairygarden.item;

import net.minecraft.world.level.block.BonemealSource;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.sugarplumstudios.fairygarden.block.MoonflowerBlock;
import com.sugarplumstudios.fairygarden.block.StarbloomCropBlock;
import com.sugarplumstudios.fairygarden.registry.ModParticles;
import com.sugarplumstudios.fairygarden.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

/**
 * Fairy dust is a magical fertiliser: starbloom ripens at once (even by day), moonflowers open on the spot,
 * and any other plant gets three rounds of bone-meal growth.
 */
public class FairyDustItem extends Item {
    public FairyDustItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        boolean applicable;
        if (block instanceof StarbloomCropBlock crop) {
            applicable = !crop.isMaxAge(state);
        } else if (block instanceof MoonflowerBlock) {
            applicable = !state.getValue(MoonflowerBlock.OPEN);
        } else {
            applicable = block instanceof BonemealableBlock bonemealable && bonemealable.isValidBonemealTarget(level, pos, state, BonemealSource.INTERACTION);
        }
        if (!applicable) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            if (block instanceof StarbloomCropBlock crop) {
                server.setBlock(pos, crop.getStateForAge(crop.getMaxAge()), Block.UPDATE_CLIENTS);
            } else if (block instanceof MoonflowerBlock) {
                server.setBlock(pos, state.setValue(MoonflowerBlock.OPEN, true), Block.UPDATE_ALL);
            } else if (block instanceof BonemealableBlock bonemealable) {
                for (int i = 0; i < 3; i++) {
                    BlockState current = server.getBlockState(pos);
                    if (current.getBlock() != block || !bonemealable.isValidBonemealTarget(server, pos, current, BonemealSource.INTERACTION)) {
                        break;
                    }
                    bonemealable.performBonemeal(server, server.getRandom(), pos, current, BonemealSource.INTERACTION);
                }
            }
            server.sendParticles(ModParticles.FAIRY_SPARKLE.get(), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                    14, 0.35, 0.35, 0.35, 0.02);
            server.playSound(null, pos, ModSounds.FAIRY_DUST_USE.get(), SoundSource.BLOCKS, 0.8F, 0.9F + server.getRandom().nextFloat() * 0.3F);
            Player player = context.getPlayer();
            server.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            if (player == null || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.fairygarden.fairy_dust.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
