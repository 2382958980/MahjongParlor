package com.candle.mahjongparlor.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class CucumberItem extends Item {

    // 定义一个固定的UUID，用于攻击力属性修改器
    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("d849e2b1-5bb8-4b7c-a0c5-8968b609e21f");
    private final float attackDamage;

    public CucumberItem(Properties properties, float attackDamage) {
        super(properties);
        this.attackDamage = attackDamage;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        // 当物品在主手时，添加攻击力属性
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            // 首先调用父类的方法，以保留可能存在的默认属性
            builder.putAll(super.getAttributeModifiers(slot, stack));
            // 添加攻击力属性
            builder.put(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                            ATTACK_DAMAGE_MODIFIER_UUID,
                            "Weapon modifier", // 描述名
                            this.attackDamage, // 攻击力数值
                            AttributeModifier.Operation.ADDITION // 操作类型：直接加法
                    )
            );
            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }
}