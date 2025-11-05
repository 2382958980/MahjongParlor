package com.candle.mahjongparlor.event;

import com.candle.mahjongparlor.item.HormoneGunItem;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "mahjongparlor", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerMovementHandler {

    // 使用固定UUID标识这个属性修改器（重要：确保唯一性）
    private static final UUID ADS_SPEED_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String ADS_SPEED_MODIFIER_NAME = "hormone_gun_ads_slowdown";
    private static final double SPEED_MULTIPLIER = -0.5; // 减速50%（-0.5表示乘以0.5）

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 只在服务端处理，且只在阶段开始时处理（避免重复）
        if (event.phase != TickEvent.Phase.START) return;

        Player player = event.player;
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        boolean isAiming = HormoneGunItem.isPlayerAiming(player);
        AttributeModifier existingModifier = movementSpeed.getModifier(ADS_SPEED_MODIFIER_UUID);

        if (isAiming) {
            // 取消疾跑状态
            if (player.isSprinting()) {
                player.setSprinting(false);
            }

            // 添加速度修改器（如果还没有）
            if (existingModifier == null) {
                AttributeModifier modifier = new AttributeModifier(
                        ADS_SPEED_MODIFIER_UUID,
                        ADS_SPEED_MODIFIER_NAME,
                        SPEED_MULTIPLIER,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                );
                movementSpeed.addTransientModifier(modifier);
            }
        } else {
            // 移除速度修改器
            if (existingModifier != null) {
                movementSpeed.removeModifier(ADS_SPEED_MODIFIER_UUID);
            }
        }
    }
}