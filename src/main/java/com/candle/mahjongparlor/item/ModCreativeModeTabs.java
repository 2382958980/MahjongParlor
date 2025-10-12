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

    public static final RegistryObject<CreativeModeTab> MG_TAB =
            CREATIVE_MODE_TABS.register("mg_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.CUCUMBER.get()))
                    .title(Component.translatable("itemGroup.mg_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModBlocks.LAVA_SINK_BLOCK.get());
                        pOutput.accept(ModItems.CUCUMBER.get());
                    })
                    .build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
