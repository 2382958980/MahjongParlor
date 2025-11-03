package com.candle.mahjongparlor.entity.projectile;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag; // 新增导入
import net.minecraft.network.syncher.EntityDataAccessor; // 新增导入
import net.minecraft.network.syncher.EntityDataSerializers; // 新增导入
import net.minecraft.network.syncher.SynchedEntityData; // 新增导入
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class magnetbombproj extends ThrowableItemProjectile {

    private static final EntityDataAccessor<Boolean> DATA_STUCK_TO_BLOCK =
            SynchedEntityData.defineId(magnetbombproj.class, EntityDataSerializers.BOOLEAN);

    private int fuseTime = 60; // 爆炸倒计时
    // private boolean isStuckToBlock = false; // <-- 移除旧的、未同步的字段

    public magnetbombproj(EntityType<? extends magnetbombproj> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public magnetbombproj(Level pLevel, LivingEntity pShooter) {
        super(ModEntityTypes.MAGNETBOMB.get(), pShooter, pLevel);
    }

    public magnetbombproj(Level pLevel, double pX, double pY, double pZ) {
        super(ModEntityTypes.MAGNETBOMB.get(), pX, pY, pZ, pLevel);
    }

    // 步骤 2: 覆写 defineSynchedData 方法来注册我们的数据
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STUCK_TO_BLOCK, false); // 注册并设置初始值为 false
    }

    // 步骤 3 (推荐): 添加读写 NBT 的方法，以便在区块卸载和重载时保持状态
    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("IsStuckToBlock", this.isStuckToBlock());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setStuckToBlock(pCompound.getBoolean("IsStuckToBlock"));
    }

    // 步骤 4 (推荐): 创建 getter 和 setter 辅助方法，让代码更清晰
    public boolean isStuckToBlock() {
        return this.entityData.get(DATA_STUCK_TO_BLOCK);
    }

    public void setStuckToBlock(boolean stuck) {
        this.entityData.set(DATA_STUCK_TO_BLOCK, stuck);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        Entity entity = pResult.getEntity();

        // 步骤 5: 使用新的 isStuckToBlock() 方法进行检查
        if (!this.level().isClientSide && !this.isPassenger() && !this.isStuckToBlock()) {
            this.startRiding(entity, true);
            this.setNoGravity(true);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_BLOCK_PLACE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);

        if (!this.level().isClientSide && !this.isStuckToBlock() && !this.isPassenger()) {
            this.setStuckToBlock(true);
            this.setNoGravity(true);

            // --- 进阶优化版本 ---

            // 定义一个极小的偏移量
            final double OFFSET = 0.005D;

            // 1. 获取精确的撞击点和撞击方向
            Vec3 hitLocation = pResult.getLocation();
            Direction hitDirection = pResult.getDirection();

            // 2. 根据撞击方向，在撞击点上增加一个微小的偏移
            double newX = hitLocation.x + (hitDirection.getStepX() * OFFSET);
            double newY = hitLocation.y + (hitDirection.getStepY() * OFFSET);
            double newZ = hitLocation.z + (hitDirection.getStepZ() * OFFSET);

            // 3. 将抛射物的位置设置为这个微调后的位置
            this.setPos(newX, newY, newZ);

            // 4. 速度归零
            this.setDeltaMovement(Vec3.ZERO);

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.NETHERITE_BLOCK_STEP, SoundSource.NEUTRAL, 0.5F, 1.2F);
        }
    }

    @Override
    public void tick() {
        // 步骤 7: 使用新的 isStuckToBlock() 方法进行检查
        if (this.isPassenger() || this.isStuckToBlock()) {
            // ... 内部逻辑保持不变，因为现在客户端也能正确获取到 isStuckToBlock() 的状态了 ...
            if (this.isPassenger()) {
                Entity vehicle = this.getVehicle();
                if (vehicle == null || !vehicle.isAlive() || vehicle.isRemoved()) {
                    this.stopRiding();
                    explode();
                    this.discard();
                    return;
                }
            }

            if (fuseTime > 0) {
                fuseTime--;
                if (this.level().isClientSide) {
                    this.level().addParticle(ParticleTypes.SMOKE,
                            this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D),
                            0.0D, 0.0D, 0.0D);
                }
            } else {
                if (this.isPassenger()) {
                    this.stopRiding();
                }
                explode();
                this.discard();
            }
        } else {
            super.tick();
        }
    }

    private void explode() {
        if (!this.level().isClientSide) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 3.0F, Level.ExplosionInteraction.NONE);
        }
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
    }

    @Override
    protected boolean canRide(Entity pVehicle) {
        return true;
    }
}