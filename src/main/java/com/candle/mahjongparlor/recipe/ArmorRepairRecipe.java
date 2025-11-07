package com.candle.mahjongparlor.recipe;

import com.candle.mahjongparlor.item.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.*;


public class ArmorRepairRecipe extends CustomRecipe {

    private static final String TAG_MULTIPLIER = "DurabilityMultiplier";
    private static final double MULTIPLIER_DECAY = 0.9d; // 每次乘以 0.9（减少 10%）
    private static final double DEFAULT_MULTIPLIER = 1.0d;
    private static final int MIN_EFFECTIVE_MAX = 1; // 最小最大耐久保护值

    public ArmorRepairRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        boolean foundArmor = false;
        boolean foundCore = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof ArmorItem && stack.isDamageableItem()) {
                if (foundArmor) return false;
                foundArmor = true;
            } else if (stack.is(ModItems.REPAIR_CORE.get())) {
                if (foundCore) return false;
                foundCore = true;
            } else {
                return false;
            }
        }
        return foundArmor && foundCore;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
        ItemStack armorCopy = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
                armorCopy = stack.copy(); // 保留 NBT
                break;
            }
        }

        if (armorCopy.isEmpty()) return ItemStack.EMPTY;

        CompoundTag tag = armorCopy.getOrCreateTag();

        // base max = 物品定义的原始最大耐久（不包含 NBT）
        int baseMax = armorCopy.getItem().getMaxDamage();

        // 读取 multiplier（若无则为 1.0）
        double multiplier = DEFAULT_MULTIPLIER;
        if (tag.contains(TAG_MULTIPLIER)) {
            multiplier = tag.getDouble(TAG_MULTIPLIER);
            if (multiplier <= 0) multiplier = DEFAULT_MULTIPLIER;
        }

        // 当前生效的最大耐久（向下取整），并做最小保护
        int currentMax = (int) Math.max(MIN_EFFECTIVE_MAX, Math.floor(baseMax * multiplier));

        // === 1) 修复当前耐久：修复量 = 当前生效最大耐久的 30%（向上取整） ===
        int repairAmount = (int) Math.ceil(currentMax * 0.30);
        int currentDamage = armorCopy.getDamageValue();
        int newDamage = Math.max(0, currentDamage - repairAmount);

        // === 2) 扣除 10% 最大耐久：multiplier *= 0.9（并写回 NBT） ===
        double newMultiplier = multiplier * MULTIPLIER_DECAY;
        int newMax = (int) Math.max(MIN_EFFECTIVE_MAX, Math.floor(baseMax * newMultiplier));
        // 将 newMultiplier 保存进 NBT（持久化）
        tag.putDouble(TAG_MULTIPLIER, newMultiplier);

        // === 3) 如果当前 damage 在 newMax 范围外，做友好处理：把 damage 限制到 newMax - 1，避免合成直接毁掉装备 ===
        //    注意：ItemStack 判定破损通常是 damage >= maxDamage，因此把 damage 最大设为 newMax - 1 保证不破损。
        if (newDamage >= newMax) {
            newDamage = Math.max(0, newMax - 1);
        }

        armorCopy.setDamageValue(newDamage);
        armorCopy.setTag(tag);
        return armorCopy;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ARMOR_REPAIR_SERIALIZER.get();
    }
}

