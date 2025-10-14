package com.candle.mahjongparlor.item;

import com.candle.mahjongparlor.MahjongParlor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.candle.mahjongparlor.block.ModBlocks.ENHANCED_SCAFFOLDING;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MahjongParlor.MOD_ID);
    //黄瓜
    public static final RegistryObject<Item> CUCUMBER =
            ITEMS.register("cucumber", () -> new CucumberItem(
                    new Item.Properties().food((new FoodProperties.Builder()).nutrition(2).saturationMod(0.2F).build()),
                    2.0F
            ));
    //
    public static final RegistryObject<Item> WATERBOWL =
            ITEMS.register("waterbowl", () ->new Item(new Item.Properties().stacksTo(1)));
    //鸡汁
    public static final RegistryObject<Item> CHICKENSOUP =
            ITEMS.register("chickensoup", () ->new BowlFoodItem(new Item.Properties().stacksTo(1).food(stew(7).build())));

    public static final RegistryObject<Item> IRONBASIN =
            ITEMS.register("ironbasin", () ->new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPICYHOTPOT =
            ITEMS.register("spicyhotpot", () ->new BasinFoodItem(new Item.Properties().stacksTo(1).food(stew(10).effect(() -> new MobEffectInstance(MobEffects.SATURATION, 20, 0), 1.0f).build())));

    //增强型脚手架
    public static final RegistryObject<Item> ENHANCED_SCAFFOLDING_ITEM = ITEMS.register("enhanced_scaffolding",
            () -> new EnhancedScaffoldingBlockItem(
                    ENHANCED_SCAFFOLDING.get(),
                    new Item.Properties()
            )
    );
    //地瓜
    public static final RegistryObject<Item>  PACHYRHIZUS =
            ITEMS.register("pachyrhizus",() -> new Item(new Item.Properties().stacksTo(64).food(stew(2).build())));

    private static FoodProperties.Builder stew(int pNutrition) {
        return (new FoodProperties.Builder()).nutrition(pNutrition).saturationMod(0.6F);
    }
    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
