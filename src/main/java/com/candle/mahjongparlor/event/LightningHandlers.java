package com.candle.mahjongparlor.event;

import com.candle.mahjongparlor.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(modid = "mahjongparlor")
public class LightningHandlers {

    @SubscribeEvent
    public static void onLightningStrikeTransform(EntityJoinLevelEvent event) {
        // 1. 前置检查 (Guard Clauses)
        // ----------------------------------------------------

        // 确保只在服务器端运行，客户端不处理逻辑
        if (event.getLevel().isClientSide()) {
            return;
        }

        // 确保生成的实体是闪电，忽略所有其他实体
        if (event.getEntity().getType() != EntityType.LIGHTNING_BOLT) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        Vec3 lightningPos = event.getEntity().position();

        AABB searchBox = new AABB(lightningPos, lightningPos).inflate(4.0);

        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(ItemEntity.class, searchBox);

        for (ItemEntity oldItemEntity : nearbyItems) {
            // 检查这个物品实体是否是我们想要转化的目标
            if (oldItemEntity.getItem().getItem() == Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {

                if (oldItemEntity.position().distanceTo(lightningPos) <= 3.0) {

                    int count = oldItemEntity.getItem().getCount();
                    Vec3 itemPos = oldItemEntity.position();

                    oldItemEntity.discard();

                    ItemStack newStack = new ItemStack(ModItems.GLORYMODEL.get(), count);
                    ItemEntity newItemEntity = new ItemEntity(level, itemPos.x, itemPos.y, itemPos.z, newStack);

                    // e. 设置新物品实体的属性
                    newItemEntity.setPickUpDelay(20); // 短暂的拾取延迟，防止被瞬间吸走
                    newItemEntity.setUnlimitedLifetime(); // 设置为永不消失
                    newItemEntity.setInvulnerable(true);//物品不会因为雷击等伤害消失
                    // f. 将新的物品实体添加到世界中
                    level.addFreshEntity(newItemEntity);

                    // g. 播放视觉和听觉效果
                    level.sendParticles(ParticleTypes.FLASH, itemPos.x, itemPos.y + 0.5, itemPos.z, 3, 0, 0, 0, 0);
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, itemPos.x, itemPos.y + 0.5, itemPos.z, 50, 0.4, 0.4, 0.4, 0.2);
                    level.playSound(null, itemPos.x, itemPos.y, itemPos.z, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    break;
                }
            }
        }
    }
}