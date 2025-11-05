package com.candle.mahjongparlor;

import com.candle.mahjongparlor.block.ModBlocks;
import com.candle.mahjongparlor.entity.projectile.ModEntityTypes;
import com.candle.mahjongparlor.event.HealBulletRenderer;
import com.candle.mahjongparlor.item.ModCreativeModeTabs;
import com.candle.mahjongparlor.item.ModItems;
import com.candle.mahjongparlor.loot.ModLootModifiers;

import com.candle.mahjongparlor.network.ModNetworking;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MahjongParlor.MOD_ID)
public class MahjongParlor
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "mahjongparlor";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public MahjongParlor(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        ModNetworking.register();
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        // 注册Loot Modifiers
        ModLootModifiers.register(modEventBus);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            EntityRenderers.register(ModEntityTypes.MAGNETBOMB.get(), ThrownItemRenderer::new);
            EntityRenderers.register(ModEntityTypes.HEAL_BULLET.get(), HealBulletRenderer::new);

        }
    }
}
