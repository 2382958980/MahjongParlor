package com.candle.mahjongparlor.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// 注册到 Forge 事件总线
@Mod.EventBusSubscriber(modid = "mahjongparlor", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WordMsg {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // 获取玩家实体
        Player player = event.getEntity();

        // 创建消息内容
        Component message = Component.literal("免责声明：本mod由银烛主开发，没有一个yy受到伤害。")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);

        // 发送给玩家
        player.sendSystemMessage(message);
    }
}