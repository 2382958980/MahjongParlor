package com.candle.mahjongparlor.entity.projectile;// 文件路径: .../mahjongparlor/core/init/ModEntityTypes.java (推荐的路径)


import com.candle.mahjongparlor.MahjongParlor; // 你的主类
import com.candle.mahjongparlor.entity.projectile.magnetbombproj; // 你的实体类
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

    // 1. 创建一个 DeferredRegister 实例
    //    它会注册 EntityType，并将其绑定到你的 Mod ID 上。
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MahjongParlor.MOD_ID);

    // 2. 定义并注册你的粘弹实体类型
    public static final RegistryObject<EntityType<magnetbombproj>> MAGNETBOMB =
            ENTITY_TYPES.register("magnet_bomb", // 这是注册名，必须是小写且唯一
                    () -> EntityType.Builder.<magnetbombproj>of(magnetbombproj::new, MobCategory.MISC) // a. 工厂方法 和 b. 分类
                            .sized(0.25F, 0.25F) // c. 碰撞箱大小 (宽, 高)
                            .clientTrackingRange(4) // d. 客户端追踪范围
                            .updateInterval(10) // e. 数据同步频率
                            .build("magnet_bomb")); // f. 和注册名保持一致
    public static final RegistryObject<EntityType<HealBullet>> HEAL_BULLET =
            ENTITY_TYPES.register("heal_bullet",
                    () -> EntityType.Builder.<HealBullet>of(HealBullet::new, MobCategory.MISC)
                            .sized(0.2F, 0.2F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("heal_bullet")
            );


    // 3. 创建一个方法，由你的主类调用，以将 DeferredRegister 附加到 Mod 事件总线
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}