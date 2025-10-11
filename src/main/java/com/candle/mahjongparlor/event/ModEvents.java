package com.candle.mahjongparlor.event;

import com.candle.mahjongparlor.item.ModItems; // 替换成你的物品注册类
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.ForgeMod;

// 告诉Forge这个类包含了事件监听器
@Mod.EventBusSubscriber(modid = "mahjongparlor") // 替换成你的Mod ID
public class ModEvents {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (!stack.is(Items.BOWL)) {
            return;
        }

        // 修正 #2: 调用我们自己的、复制过来的射线检测方法
        BlockHitResult rayTraceResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = rayTraceResult.getBlockPos();

            if (!level.mayInteract(player, blockpos)) {
                return;
            }

            if (level.getFluidState(blockpos).is(FluidTags.WATER)) {
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.FLUID_PICKUP, blockpos);

                ItemStack waterBowlStack = new ItemStack(ModItems.WATERBOWL.get());
                turnBowlIntoWaterBowl(player, event.getHand(), stack, waterBowlStack);

                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    // 辅助方法：执行物品转换
    public static void turnBowlIntoWaterBowl(Player player, InteractionHand hand, ItemStack emptyBowlStack, ItemStack filledBowlStack) {
        player.awardStat(Stats.ITEM_USED.get(emptyBowlStack.getItem()));

        if (!player.getAbilities().instabuild) {
            emptyBowlStack.shrink(1);
        }

        if (emptyBowlStack.isEmpty()) {
            player.setItemInHand(hand, filledBowlStack);
        } else if (!player.getInventory().add(filledBowlStack)) {
            player.drop(filledBowlStack, false);
        }
    }

    private static BlockHitResult getPlayerPOVHitResult(Level pLevel, Player pPlayer, ClipContext.Fluid pFluidHandling) {
        float xRot = pPlayer.getXRot();
        float yRot = pPlayer.getYRot();
        Vec3 eyePos = pPlayer.getEyePosition();
        float f2 = Mth.cos(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = Mth.sin(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -Mth.cos(-xRot * ((float)Math.PI / 180F));
        float f5 = Mth.sin(-xRot * ((float)Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;

        // --- 这就是关键的修正 ---
        // 使用 Forge 的属性系统来获取玩家的触及距离，而不是调用一个不存在的方法
        double reach = pPlayer.getAttributeValue(ForgeMod.BLOCK_REACH.get());

        Vec3 endVec = eyePos.add((double)f6 * reach, (double)f5 * reach, (double)f7 * reach);
        return pLevel.clip(new ClipContext(eyePos, endVec, ClipContext.Block.OUTLINE, pFluidHandling, pPlayer));
    }
}