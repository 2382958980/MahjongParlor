package com.candle.mahjongparlor.event;

import com.candle.mahjongparlor.item.HormoneGunItem;
import com.candle.mahjongparlor.network.ModNetworking;
import com.candle.mahjongparlor.network.ShootPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "mahjongparlor", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class GunShootHandler {

    /**
     * ✅ 使用 InputEvent 拦截更早期的输入，可以阻止摆手动画
     */
    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        // 只处理攻击动作（左键）
        if (!event.isAttack()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack mainStack = player.getMainHandItem();
        ItemStack offStack = player.getOffhandItem();

        InteractionHand hand = null;
        HormoneGunItem gun = null;
        ItemStack gunStack = null;

        // 检查主手
        if (mainStack.getItem() instanceof HormoneGunItem mainGun) {
            gun = mainGun;
            gunStack = mainStack;
            hand = InteractionHand.MAIN_HAND;
        }
        // 检查副手
        else if (offStack.getItem() instanceof HormoneGunItem offGun) {
            gun = offGun;
            gunStack = offStack;
            hand = InteractionHand.OFF_HAND;
        }

        // 如果持有枪械
        if (gun != null && hand != null) {
            // 检查冷却
            if (!gun.canShoot(gunStack, player)) {
                // ✅ 冷却中：取消事件（阻止摆手动画）
                event.setCanceled(true);
                event.setSwingHand(false);
                return;
            }

            // 冷却结束：发送射击包
            ModNetworking.CHANNEL.sendToServer(new ShootPacket(hand));

            // ✅ 取消默认攻击动作（阻止破坏方块等）
            event.setCanceled(true);
            // 允许摆手动画（因为射击成功）
            event.setSwingHand(true);
        }
    }
}