package com.candle.mahjongparlor.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class portableowlBlock extends Block {
    public portableowlBlock(BlockBehaviour.Properties pProperties){
        super(pProperties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos,
                                 Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        // 只在服务端执行
        if (!pLevel.isClientSide) {
            // 创建可点击的文本组件
            MutableComponent linkText = Component.literal("§l§7[点此获取随机色图]")
                    .setStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent(
                                    ClickEvent.Action.OPEN_URL,
                                    "http://acg.yaohud.cn/dm/acg.php"))  // 替换为你的URL
                            .withUnderlined(true));
            pPlayer.sendSystemMessage(linkText);
        }
        return InteractionResult.SUCCESS;
    }
}