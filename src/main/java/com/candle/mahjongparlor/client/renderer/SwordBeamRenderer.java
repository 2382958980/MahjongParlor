package com.candle.mahjongparlor.client.renderer;

import com.candle.mahjongparlor.entity.projectile.SwordBeamEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SwordBeamRenderer extends EntityRenderer<SwordBeamEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("mahjongparlor", "textures/entity/sword_beam.png");

    public SwordBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SwordBeamEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 1. 旋转和定位
        // 让贴图朝向实体的飞行方向
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot() - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getXRot()));

        // 2. 开始绘制
        // 我们画一个简单的矩形平面，贴上我们的半圆环贴图
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        // 调整大小 (假设剑气宽 1.5 米)
        float size = 1.5f;

        // 绘制四个顶点 (画一个平面)
        // 这里的坐标系是基于实体中心的
        vertex(vertexConsumer, poseMatrix, normalMatrix, -size, 0, -size, 0, 1, packedLight);
        vertex(vertexConsumer, poseMatrix, normalMatrix, size, 0, -size, 1, 1, packedLight);
        vertex(vertexConsumer, poseMatrix, normalMatrix, size, 0, size, 1, 0, packedLight);
        vertex(vertexConsumer, poseMatrix, normalMatrix, -size, 0, size, 0, 0, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, float x, float y, float z, float u, float v, int light) {
        consumer.vertex(pose, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light) // 这里的 light 如果设为 15728880 (即 240, 240) 可以让剑气全亮发光
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(SwordBeamEntity entity) {
        return TEXTURE;
    }
}