package com.candle.mahjongparlor.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.candle.mahjongparlor.entity.projectile.SwordBeamEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;

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

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand); // 开始蓄力
        return InteractionResultHolder.consume(stack);
    }

    // 2. 设置最大蓄力时间 (72000 tick = 1小时，即无限)
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    // 3. 设置蓄力时的动作 (BOW: 拉弓动作, SPEAR: 投掷动作, BLOCK: 格挡动作)
    // 推荐用 BOW 或 SPEAR
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    // 4. 玩家松开右键时触发
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;

        int duration = this.getUseDuration(stack) - timeCharged;

        // 最小蓄力时间检查 (比如至少蓄力 10 tick / 0.5秒 才能发波)
        if (duration >= 10) {
            if (!level.isClientSide) {
                // 创建并发射剑气实体
                SwordBeamEntity beam = new SwordBeamEntity(player, level);

                // 设速度：参数分别对应 (X, Y, Z, 速度, 精准度)
                // 这里的 shootFromRotation 会根据玩家视线方向设置速度
                beam.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);

                level.addFreshEntity(beam);

                // 消耗耐久
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }
        }
    }
}
