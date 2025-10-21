package com.candle.mahjongparlor.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EternalGlorySword extends SwordItem {
    public EternalGlorySword() {
        super(Tiers.NETHERITE, 9, -2.2F, new Item.Properties().fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 不掉耐久
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        // 不掉耐久
        return true;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false; // 不可损坏
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        stack.getOrCreateTag().putBoolean("Unbreakable", true); // 加显示标签
    }
}
