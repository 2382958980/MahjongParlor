package com.candle.mahjongparlor.event;

import com.candle.mahjongparlor.entity.projectile.HealBullet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HealBulletRenderer extends EntityRenderer<HealBullet> {
    public HealBulletRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(HealBullet entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {

    }
    @Override
    public ResourceLocation getTextureLocation(HealBullet entity) {
        return null;
    }
}
