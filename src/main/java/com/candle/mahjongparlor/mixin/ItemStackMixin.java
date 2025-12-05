package com.candle.mahjongparlor.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    private static final String TAG_MULTIPLIER = "DurabilityMultiplier";

    // 改用 RETURN 注入：先让游戏算出原始最大耐久，我们再修改它
    @Inject(method = "getMaxDamage", at = @At("RETURN"), cancellable = true)
    private void modifyMaxDamage(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        // 1. 基础检查
        if (!stack.hasTag()) return;
        CompoundTag tag = stack.getTag();
        if (!tag.contains(TAG_MULTIPLIER)) return;

        // 2. 读取倍率
        double mult = tag.getDouble(TAG_MULTIPLIER);
        if (mult <= 0) mult = 1.0d;

        // 3. 获取原始返回值（这就是原版算出的最大耐久）
        int originalMax = cir.getReturnValue();

        // 4. 计算新值
        int newMax = (int) Math.max(1, Math.floor(originalMax * mult));

        // 5. 暴力调试打印 (非常重要！！！)
        // 打开游戏控制台，如果没看到这行字，说明 Mixin 根本没加载！
        // System.out.println("Mixin Run! Item: " + stack.getItem() + " | Old: " + originalMax + " | New: " + newMax);

        // 6. 覆盖返回值
        cir.setReturnValue(newMax);
    }
}