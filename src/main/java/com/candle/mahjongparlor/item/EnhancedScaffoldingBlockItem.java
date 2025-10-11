package com.candle.mahjongparlor.item;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ScaffoldingBlockItem; // ★★★ 关键：之前可能是 BlockItem
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

// ★★★ 关键：继承自 ScaffoldingBlockItem 而不是 BlockItem ★★★
public class EnhancedScaffoldingBlockItem extends ScaffoldingBlockItem {

    public EnhancedScaffoldingBlockItem(Block pBlock, Item.Properties pProperties) {
        super(pBlock, pProperties);
    }

    // ★★★ 关键：覆写放置方法以移除水平限制 ★★★
    @Override
    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext pContext) {
        BlockPos blockpos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        BlockState blockstate = level.getBlockState(blockpos);
        Block block = this.getBlock();

        // 当点击的不是脚手架时，我们允许放置，所以直接返回 pContext
        if (!blockstate.is(block)) {
            return pContext;
        } else {
            // --- 以下是原版 ScaffoldingBlockItem 的逻辑 ---
            Direction direction;
            if (pContext.isSecondaryUseActive()) {
                direction = pContext.isInside() ? pContext.getClickedFace().getOpposite() : pContext.getClickedFace();
            } else {
                direction = pContext.getClickedFace() == Direction.UP ? pContext.getHorizontalDirection() : Direction.UP;
            }

            BlockPos.MutableBlockPos mutablePos = blockpos.mutable().move(direction);

            // --- ★★★ 这是我们的核心修改 ★★★ ---
            // 原版的循环是 while(i < 7)，我们改为无限循环，直到碰到障碍或边界
            while (true) {
                if (!level.isClientSide && !level.isInWorldBounds(mutablePos)) {
                    Player player = pContext.getPlayer();
                    int j = level.getMaxBuildHeight();
                    if (player instanceof ServerPlayer && mutablePos.getY() >= j) {
                        ((ServerPlayer) player).sendSystemMessage(Component.translatable("build.tooHigh", j - 1).withStyle(ChatFormatting.RED), true);
                    }
                    break;
                }

                blockstate = level.getBlockState(mutablePos);
                if (!blockstate.is(this.getBlock())) {
                    if (blockstate.canBeReplaced(pContext)) {
                        return BlockPlaceContext.at(pContext, mutablePos, direction);
                    }
                    break;
                }

                mutablePos.move(direction);
                // 我们移除了原版的计数器 i，因为它不再需要了
            }

            return null;
        }
    }
}