
package com.candle.mahjongparlor.recipe;

import com.candle.mahjongparlor.MahjongParlor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MahjongParlor.MOD_ID);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<ArmorRepairRecipe>> ARMOR_REPAIR_SERIALIZER =
            SERIALIZERS.register("armor_repair", () -> new SimpleCraftingRecipeSerializer<>(ArmorRepairRecipe::new));

    public static void register(net.minecraftforge.eventbus.api.IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
