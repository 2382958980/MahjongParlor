package com.candle.mahjongparlor.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

/**
 * HealBulletEntity - 直线飞行、无重力、带绿色粒子拖尾。
 * 命中生物时立即治疗并给予 30 秒再生效果。
 *
 * 注意：把 ModEntities.HEAL_BULLET 替换为你注册实体的引用。
 */
public class HealBullet extends Projectile {

    public HealBullet(EntityType<? extends HealBullet> type, Level level) {
        super(type, level);
    }

    // 便捷构造：服务器端创建并跟随射手位置
    public HealBullet(Level level, LivingEntity shooter) {
        this(ModEntityTypes.HEAL_BULLET.get(), level); // 替换为你自己的 RegistryObject 引用
        this.setOwner(shooter);
        this.setPos(shooter.getEyePosition(1.0F));
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public boolean isNoGravity() {
        return true; // 关闭重力
    }

    @Override
    public void tick() {
        // 我们自己控制移动和碰撞，不直接依赖可能会有内部调用私有方法的工具
        Vec3 start = this.position();
        Vec3 motion = this.getDeltaMovement();
        Vec3 end = start.add(motion);

        // --- 实体碰撞检测（手工实现，兼容各版本） ---
        // 收集可能被击中的实体（在 start->end 路径上的实体）
        // AABB 扩展一点，避免过薄目标漏判
        AABB box = this.getBoundingBox().expandTowards(motion).inflate(1.0D);
        List<Entity> list = this.level().getEntities(this, box, e -> {
            if (e == this) return false;
            if (!(e instanceof LivingEntity)) return false;
            if (!e.isAlive()) return false;
            // 你可以加更多过滤条件，比如不命中持有者等：
            Entity owner = this.getOwner();
            return owner == null || !owner.equals(e);
        });

        EntityHitResult nearestHit = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity ent : list) {
            // 使用实体包围盒的射线裁剪，取最近的交点
            AABB targetBox = ent.getBoundingBox().inflate(0.3D); // inflate 以更稳健
            Optional<Vec3> opt = targetBox.clip(start, end);
            if (opt.isPresent()) {
                double d = start.distanceTo(opt.get());
                if (d < nearestDist) {
                    nearestDist = d;
                    nearestHit = new EntityHitResult(ent, opt.get());
                }
            }
        }

        if (nearestHit != null) {
            // 命中实体
            this.onHit(nearestHit);
            return; // 命中后实体通常被销毁，提前返回以避免重复逻辑
        }

        // --- 没有命中实体，移动到 end 点 ---
        this.setPos(end.x, end.y, end.z);

        // 客户端粒子（绿色 + 偶尔心形）
        if (this.level().isClientSide) {
            // ENTITY_EFFECT 可接受三个速度参数，部分实现把它当色值近似
            this.level().addParticle(ParticleTypes.GLOW_SQUID_INK,
                    this.getX(), this.getY(), this.getZ(),
                    0, 0, 0); // 绿色
        }

        // 超龄销毁（防止永久存在）
        if (this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (result instanceof EntityHitResult ehr) {
            Entity target = ehr.getEntity();
            if (target instanceof LivingEntity living) {
                // 立即治疗（可按需调整数值）
                living.heal(4.0F);

                // 30 秒再生（600 tick），级别 0 = 再生 I
                living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 0));

                // 服务器端发命中粒子（心形 + 绿色药水粒子）
                if (this.level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.EGG_CRACK,
                            living.getX(), living.getY(), living.getZ(),
                            10, 0, 0, 0, 0.0);
                }
            }
        }

        // 命中后立即销毁实体；如需穿透则修改这里
        this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    /** 辅助：从射手处创建并发射（速度以 scale 设置） */
    public static HealBullet createAndShoot(Level level, LivingEntity shooter, double speed) {
        HealBullet b = new HealBullet(level, shooter);
        Vec3 look = shooter.getLookAngle();
        b.setDeltaMovement(look.scale(speed));
        // 初始位置前移一点，避免与射手包围盒重叠
        b.setPos(shooter.getX() + look.x * 0.5, shooter.getEyeY() - 0.1 + look.y * 0.5, shooter.getZ() + look.z * 0.5);
        level.addFreshEntity(b);
        return b;
    }
}
