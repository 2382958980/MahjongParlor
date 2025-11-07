package com.candle.mahjongparlor.event;

import com.candle.mahjongparlor.MahjongParlor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@Mod.EventBusSubscriber(modid = MahjongParlor.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModClientEvents {
    private static final String TAG_MULTIPLIER = "DurabilityMultiplier";

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.hasTag() && stack.getTag().contains(TAG_MULTIPLIER)) {
            double mult = stack.getTag().getDouble(TAG_MULTIPLIER);
            if (mult <= 0) mult = 1.0d;
            double reducedPercent = (1.0d - mult) * 100.0d;
            int displayPercent = (int) Math.round(reducedPercent);
            event.getToolTip().add(Component.literal("§7Max Durability Reduced: §c-" + displayPercent + "%"));
        }
    }
}
