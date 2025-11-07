package com.candle.mahjongparlor.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.nbt.CompoundTag;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    private static final String TAG_MULTIPLIER = "DurabilityMultiplier";
    private static final double DEFAULT_MULTIPLIER = 1.0d;
    private static final int MIN_EFFECTIVE_MAX = 1;

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void injected(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains(TAG_MULTIPLIER)) {
                double mult = tag.getDouble(TAG_MULTIPLIER);
                if (mult <= 0) mult = DEFAULT_MULTIPLIER;
                int baseMax = stack.getItem().getMaxDamage();
                int effectiveMax = (int) Math.max(MIN_EFFECTIVE_MAX, Math.floor(baseMax * mult));
                cir.setReturnValue(effectiveMax);
            }
        }
    }
}
