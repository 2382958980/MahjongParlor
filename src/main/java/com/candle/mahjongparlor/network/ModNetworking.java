package com.candle.mahjongparlor.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("mahjongparlor", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(ShootPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ShootPacket::encode)
                .decoder(ShootPacket::decode)
                .consumerMainThread(ShootPacket::handle)
                .add();
    }
}