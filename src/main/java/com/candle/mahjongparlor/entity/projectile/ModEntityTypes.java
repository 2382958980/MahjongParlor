package com.candle.mahjongparlor.entity.projectile; // 保持你原有的包路径

import com.candle.mahjongparlor.MahjongParlor;
import com.candle.mahjongparlor.entity.projectile.SwordBeamEntity;
import com.candle.mahjongparlor.entity.projectile.magnetbombproj;
// import com.candle.mahjongparlor.entity.projectile.HealBullet; // 如果 HealBullet 在同包下则不需要导入，否则需要

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MahjongParlor.MOD_ID);

    // ==========================================
    // 现有的实体
    // ==========================================

    public static final RegistryObject<EntityType<magnetbombproj>> MAGNETBOMB =
            ENTITY_TYPES.register("magnet_bomb",
                    () -> EntityType.Builder.<magnetbombproj>of(magnetbombproj::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("magnet_bomb"));

    public static final RegistryObject<EntityType<HealBullet>> HEAL_BULLET =
            ENTITY_TYPES.register("heal_bullet",
                    () -> EntityType.Builder.<HealBullet>of(HealBullet::new, MobCategory.MISC)
                            .sized(0.2F, 0.2F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("heal_bullet")
            );

    // ==========================================
    // 新增：剑气实体 (Sword Beam)
    // ==========================================

    public static final RegistryObject<EntityType<SwordBeamEntity>> SWORD_BEAM =
            ENTITY_TYPES.register("sword_beam", // 注册ID，也是 /summon mahjongparlor:sword_beam 的名字
                    () -> EntityType.Builder.<SwordBeamEntity>of(SwordBeamEntity::new, MobCategory.MISC)
                            // 碰撞箱大小：宽 1.5 (匹配渲染的视觉宽度), 高 0.5 (扁平状)
                            .sized(1.5F, 0.5F)
                            // 追踪范围：64格 (标准抛射物距离)
                            .clientTrackingRange(64)
                            // 更新频率：1 (极为重要！高速飞行的物体必须每 tick 更新，否则看起来会瞬移)
                            .updateInterval(1)
                            .build("sword_beam")
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}