package com.candle.mahjongparlor.item;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;

public final class ModSmithingTemplates {
    private ModSmithingTemplates() {}

    /**
     * 为一个 smithing_transform 类型的 json 生成对应的 SmithingTemplateItem。
     *
     * @param templateId 注册时该模板的 id（将出现在 json 的 "template.item" 中）
     * @param baseItem   json 中 "base.item"（例如 minecraft:netherite_sword）
     * @param addition   json 中 "addition.item"（例如 mahjongparlor:eternalglory）
     * @param result     json 中 "result.item"（未直接用于模板显示，但可传入以便将来扩展）
     */
    public static SmithingTemplateItem createForSmithingTransform(ResourceLocation templateId,
                                                                  ResourceLocation baseItem,
                                                                  ResourceLocation addition,
                                                                  ResourceLocation result) {
        // upgradeDescription：我们用模板本身的 item 描述键（item.<namespace>.<path>）
        Component upgradeDescription = Component.translatable(Util.makeDescriptionId("item", templateId))
                .withStyle(ChatFormatting.GRAY);


        Component appliesTo = Component.translatable(Util.makeDescriptionId("item", baseItem))
                .withStyle(ChatFormatting.BLUE);

        Component ingredients = Component.translatable(Util.makeDescriptionId("item", addition))
                .withStyle(ChatFormatting.DARK_PURPLE);

        // 基本槽与附加槽说明（可以用通用的 key）
        Component baseSlotDesc = Component.translatable("item." + templateId.getNamespace() + ".smithing_template.base_slot_description");
        Component additionsSlotDesc = Component.translatable("item." + templateId.getNamespace() + ".smithing_template.additions_slot_description");

        // 根据 baseItem 的类型选择合适的空槽占位图标（尽量覆盖常见工具/武器/护甲）
        List<ResourceLocation> baseIcons = List.of(determineBaseIcon(baseItem));

        // 根据 addition 名称选择合适的附加物占位图（简单策略：按关键字映射，否则默认 ingot）
        List<ResourceLocation> additionIcons = List.of(determineAdditionIcon(addition));

        return new SmithingTemplateItem(appliesTo, ingredients, upgradeDescription, baseSlotDesc, additionsSlotDesc, baseIcons, additionIcons);
    }

    // ---- 简单的映射函数：根据资源名里是否包含关键字选占位图 ----
    private static ResourceLocation determineBaseIcon(ResourceLocation baseItem) {
        String p = baseItem.getPath();
        if (p.contains("sword")) return new ResourceLocation("item/empty_slot_sword");
        if (p.contains("pickaxe")) return new ResourceLocation("item/empty_slot_pickaxe");
        if (p.contains("axe")) return new ResourceLocation("item/empty_slot_axe");
        if (p.contains("shovel")) return new ResourceLocation("item/empty_slot_shovel");
        if (p.contains("hoe")) return new ResourceLocation("item/empty_slot_hoe");
        if (p.contains("helmet") || p.contains("boots") || p.contains("chestplate") || p.contains("leggings")) {
            // 把护甲按类型返回对应空图标（尽量识别）
            if (p.contains("helmet")) return new ResourceLocation("item/empty_armor_slot_helmet");
            if (p.contains("chest") || p.contains("chestplate")) return new ResourceLocation("item/empty_armor_slot_chestplate");
            if (p.contains("leggings")) return new ResourceLocation("item/empty_armor_slot_leggings");
            if (p.contains("boots")) return new ResourceLocation("item/empty_armor_slot_boots");
        }
        // 默认：显示通用 ingot（如果是工具/武器/护甲以外的其它基座，仍使用 ingot 占位）
        return new ResourceLocation("item/empty_slot_ingot");
    }

    private static ResourceLocation determineAdditionIcon(ResourceLocation addition) {
        String p = addition.getPath();
        if (p.contains("ingot") || p.contains("netherite") || p.contains("glory") || p.contains("gem")) {
            return new ResourceLocation("item/empty_slot_ingot");
        }
        if (p.contains("redstone") || p.contains("dust")) return new ResourceLocation("item/empty_slot_redstone_dust");
        if (p.contains("lapis") || p.contains("lapis_lazuli")) return new ResourceLocation("item/empty_slot_lapis_lazuli");
        if (p.contains("quartz")) return new ResourceLocation("item/empty_slot_quartz");
        if (p.contains("diamond")) return new ResourceLocation("item/empty_slot_diamond");
        if (p.contains("emerald")) return new ResourceLocation("item/empty_slot_emerald");
        if (p.contains("amethyst") || p.contains("shard")) return new ResourceLocation("item/empty_slot_amethyst_shard");
        // 默认回退
        return new ResourceLocation("item/empty_slot_ingot");
    }
}
