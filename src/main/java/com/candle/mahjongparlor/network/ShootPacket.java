package com.candle.mahjongparlor.network;

import com.candle.mahjongparlor.item.HormoneGunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ShootPacket {
    private final InteractionHand hand;

    public ShootPacket(InteractionHand hand) {
        this.hand = hand;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(hand);
    }

    public static ShootPacket decode(FriendlyByteBuf buf) {
        return new ShootPacket(buf.readEnum(InteractionHand.class));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof HormoneGunItem gun) {
                gun.shoot(stack, player, hand);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}