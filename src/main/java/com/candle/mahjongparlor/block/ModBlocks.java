package com.candle.mahjongparlor.block;

import com.candle.mahjongparlor.MahjongParlor;
import com.candle.mahjongparlor.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import java.util.function.Supplier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MahjongParlor.MOD_ID);

    private static <T extends Block> void registerBlockItems(String name, RegistryObject<T> block){
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    //增强型脚手架
    public static final RegistryObject<Block> ENHANCED_SCAFFOLDING = BLOCKS.register("enhanced_scaffolding",
            () -> new EnhancedScaffoldingBlock(
                    BlockBehaviour.Properties.copy(Blocks.SCAFFOLDING)
                            .strength(0.6F, 3600000.0F)
            )
    );
    //便携式猫头鹰
    public static final RegistryObject<Block> PORTABLEOWL =
            registerBlock("portableowl", () -> new portableowlBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL)));

    //岩浆水槽(?
    public static final RegistryObject<Block> LAVA_SINK_BLOCK =
            registerBlock("lava_sink_block", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 3.0F)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block){
        RegistryObject<T> blocks = BLOCKS.register(name,block);
        registerBlockItems(name,blocks);
        return blocks;
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
