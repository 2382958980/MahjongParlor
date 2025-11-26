package com.candle.mahjongparlor.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FrostedSickle extends SwordItem {
    public FrostedSickle() {
        super(Tiers.NETHERITE, 13, -0.1F, new Properties().fireResistant().rarity(Rarity.EPIC));
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
