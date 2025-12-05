package com.candle.mahjongparlor.entity.projectile;

import com.candle.mahjongparlor.entity.projectile.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class SwordBeamEntity extends Projectile {

    public SwordBeamEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    public SwordBeamEntity(LivingEntity owner, Level level) {
        // ModEntities.SWORD_BEAM.get() 是你注册的实体类型
        super(ModEntityTypes.SWORD_BEAM.get(), level);
        this.setOwner(owner);
        // 设置初始位置在眼睛高度
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        level.playSound(null, owner.getX(), owner.getY(), owner.getZ(), net.minecraft.sounds.SoundEvents.ENDER_DRAGON_SHOOT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    protected void defineSynchedData() {
        // 如果不需要同步额外数据，留空
    }

    @Override
    public void tick() {
        super.tick();

        // 1. 碰撞检测
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        // 2. 移动逻辑
        Vec3 deltaMovement = this.getDeltaMovement();
        double d0 = this.getX() + deltaMovement.x;
        double d1 = this.getY() + deltaMovement.y;
        double d2 = this.getZ() + deltaMovement.z;
        this.setPos(d0, d1, d2);

        // 3. 粒子效果：在剑气的平面宽度上随机生成
        if (this.level().isClientSide) {
            for(int i=0;i<3;i++){
                spawnBladeParticles();
            }
        }
        // 4. 超时销毁
        if (this.tickCount > 40) {
            this.discard();
        }
    }
    // 专门提取出来的粒子生成方法，逻辑更清晰
    private void spawnBladeParticles() {
        // 1. 定义局部坐标系下的偏移量
        // 假设剑气在局部空间是平躺的（XZ平面），我们需要在它的“左右”生成粒子
        // 局部 X 轴 = 左右，局部 Z 轴 = 前后，局部 Y 轴 = 上下

        double wingSpan = 1.0D; // 剑气的一半宽度

        // 随机生成左右偏移 (局部 X)
        double localX = (this.random.nextDouble() - 0.5D) * 2.0D * wingSpan;
        // 随机生成前后微量偏移 (局部 Z)，让粒子不要排成绝对直线，有一点厚度
        double localZ = (this.random.nextDouble() - 0.5D) * 0.2D;

        // 2. 创建局部向量
        Vec3 localOffset = new Vec3(localX, 0, localZ);

        // 3. 进行旋转变换 (核心步骤)
        // Minecraft 的旋转顺序通常是：先 X轴(Pitch) 再 Y轴(Yaw)
        // 注意：Vec3.xRot/yRot 接受的是弧度，且正负号可能需要调整以匹配 Minecraft 的坐标系
        // 这里的转换公式能保证将局部坐标对齐到实体的世界朝向

        Vec3 rotatedOffset = localOffset
                .xRot((float) Math.toRadians(-this.getXRot())) // 旋转俯仰角 (Pitch)
                .yRot((float) Math.toRadians(-this.getYRot())); // 旋转偏航角 (Yaw)

        // 4. 计算最终世界坐标
        double px = this.getX() + rotatedOffset.x;
        double py = this.getY() + rotatedOffset.y + 0.1; // +0.1 修正视觉中心
        double pz = this.getZ() + rotatedOffset.z;

        // 5. 生成粒子
        // 速度设为 0，让粒子留在原地形成拖尾
        this.level().addParticle(ParticleTypes.DRAGON_BREATH, px, py, pz, 0, 0, 0);

        // 可选：为了让拖尾更明显，可以每 tick 多生成几个
        // 再生成一个镜像位置的粒子，或者只是单纯增加循环次数
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        Entity owner = this.getOwner();

        // 造成 5 点伤害
        target.hurt(this.level().damageSources().mobProjectile(this, (LivingEntity) owner), 7F);
    }

    // 碰到方块时销毁
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK) {
            this.discard();
        }
    }

    // Forge 需要这个方法来处理发包
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}