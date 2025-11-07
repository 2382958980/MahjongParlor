package com.candle.mahjongparlor.item;

import com.candle.mahjongparlor.MahjongParlor;
import com.candle.mahjongparlor.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MahjongParlor.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MG_BLOCK_TAB =
            CREATIVE_MODE_TABS.register("mg_block_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.ENHANCED_SCAFFOLDING_ITEM.get()))
                    .title(Component.translatable("itemGroup.mg_block_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModBlocks.LAVA_SINK_BLOCK.get());
                        pOutput.accept(ModBlocks.ENHANCED_SCAFFOLDING.get());
                        pOutput.accept(ModBlocks.PORTABLEOWL.get());
                    })
                    .build());
    public static final RegistryObject<CreativeModeTab> MG_FOOD_TAB =
            CREATIVE_MODE_TABS.register("mg_food_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.CUCUMBER.get()))
                    .title(Component.translatable("itemGroup.mg_food_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.ETERNALGLORY.get());
                        pOutput.accept(ModItems.GLORYMODEL.get());
                        pOutput.accept(ModItems.ETERNALGLORYSWORD.get());
                        pOutput.accept(ModItems.WATERBOWL.get());
                        pOutput.accept(ModItems.IRONBASIN.get());
                        pOutput.accept(ModItems.CUCUMBER.get());
                        pOutput.accept(ModItems.BLUEAPPLE.get());
                        pOutput.accept(ModItems.PACHYRHIZUS.get());
                        pOutput.accept(ModItems.COOKEDPACHYRHIZUS.get());
                        pOutput.accept(ModItems.CHICKENSOUP.get());
                        pOutput.accept(ModItems.SPICYHOTPOT.get());
                        pOutput.accept(ModItems.MAGNETBOMB.get());
                        pOutput.accept(ModItems.HORMONEGUN.get());
                        pOutput.accept(ModItems.AFRICAHEART.get());
                        pOutput.accept(ModItems.REPAIR_CORE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
