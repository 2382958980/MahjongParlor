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

    // 为该类创建一个 Logger 实例，用于在后台打印调试信息
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 当实体加入世界时触发。我们用它来检测闪电的生成。
     * 这个方法是“即时”的，避免了所有延迟任务带来的问题。
     */
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

        // 2. 核心逻辑
        // ----------------------------------------------------

        ServerLevel level = (ServerLevel) event.getLevel();
        Vec3 lightningPos = event.getEntity().position();

        // 定义一个以闪电为中心，半径为4的搜索区域
        AABB searchBox = new AABB(lightningPos, lightningPos).inflate(4.0);

        // 获取该区域内所有的“物品实体”(ItemEntity)
        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(ItemEntity.class, searchBox);

        for (ItemEntity oldItemEntity : nearbyItems) {
            // 检查这个物品实体是否是我们想要转化的目标
            if (oldItemEntity.getItem().getItem() == Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {

                // 再次精确检查距离，确保在3格以内
                if (oldItemEntity.position().distanceTo(lightningPos) <= 3.0) {

                    // --- 开始转化过程 ---

                    // a. 从旧物品中获取关键信息（数量和精确位置）
                    int count = oldItemEntity.getItem().getCount();
                    Vec3 itemPos = oldItemEntity.position();

                    // b. 立即销毁旧的物品实体，防止物品重复
                    oldItemEntity.discard();

                    // c. 【关键检查】获取你的自定义物品。这是最容易出错的地方！
                    ItemStack newStack = new ItemStack(Items.DIAMOND, count);
                    ItemEntity newItemEntity = new ItemEntity(level, itemPos.x, itemPos.y, itemPos.z, newStack);

                    // e. 设置新物品实体的属性
                    newItemEntity.setPickUpDelay(10); // 短暂的拾取延迟，防止被瞬间吸走
                    newItemEntity.setUnlimitedLifetime(); // 设置为永不消失

                    // f. 将新的物品实体添加到世界中
                    level.addFreshEntity(newItemEntity);

                    // g. 播放视觉和听觉效果
                    level.sendParticles(ParticleTypes.FLASH, itemPos.x, itemPos.y + 0.5, itemPos.z, 3, 0, 0, 0, 0);
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, itemPos.x, itemPos.y + 0.5, itemPos.z, 50, 0.4, 0.4, 0.4, 0.2);
                    level.playSound(null, itemPos.x, itemPos.y, itemPos.z, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

                    LOGGER.info("Successfully transformed a Netherite Upgrade into a Glory Model at " + itemPos);

                    // h. 转化成功后，用 break 跳出循环。这可以防止一道闪电同时转化多个物品堆叠，让行为更可预测。
                    break;
                }
            }
        }
    }
}