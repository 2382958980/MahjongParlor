package com.candle.mahjongparlor.item;

import com.candle.mahjongparlor.entity.projectile.HealBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class HormoneGunItem extends Item {

    private static final String TAG_ADS_STATE = "ADS_state";
    private static final String TAG_ADS_PROGRESS = "ADS_progress";
    private static final String TAG_ADS_PROGRESS_PREV = "ADS_progress_prev";
    private static final String TAG_LAST_USE_TICK = "last_use_tick";
    private static final String TAG_SHOOT_TICK = "shoot_tick";
    private static final String TAG_SHOOT_PROGRESS = "shoot_progress";
    private static final String TAG_SHOOT_PROGRESS_PREV = "shoot_progress_prev";

    // durability / cooldown tags
    private static final String TAG_DURABILITY = "durability";
    private static final String TAG_COOLDOWN_START_TICK = "cooldown_start_tick"; // 用作“上次恢复时间”/装填起点

    private static final int ADS_COOLDOWN_TICKS = 10;
    private static final int SHOOT_COOLDOWN_TICKS = 10;

    private static final float ADS_STEP_PER_TICK = 0.125f;
    private static final float SHOOT_DECAY_FACTOR = 0.75f;

    // 弹药数上限与装填间隔（30 秒 = 20 * 30 tick）
    private static final int MAX_DURABILITY = 3;
    private static final int RESTORE_INTERVAL_TICKS = 20 * 30; // 30s

    public HormoneGunItem(Properties props) {
        super(props);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag tag = stack.getOrCreateTag();
        int dur = MAX_DURABILITY;
        if (tag.contains(TAG_DURABILITY)) {
            dur = tag.getInt(TAG_DURABILITY);
        }

        tooltip.add(Component.literal("弹药数: " + dur + "/" + MAX_DURABILITY)
                .withStyle(ChatFormatting.GREEN));

        // 仅当存在装填计时并且弹药未满时显示到下一次装填的剩余时间
        if (tag.contains(TAG_COOLDOWN_START_TICK) && dur < MAX_DURABILITY) {
            Level clientLevel = level;
            if (clientLevel == null) clientLevel = Minecraft.getInstance().level;

            if (clientLevel != null) {
                long lastRestoreTick = tag.getLong(TAG_COOLDOWN_START_TICK);
                long now = clientLevel.getGameTime();
                long elapsedSinceLast = now - lastRestoreTick;
                // 计算到下一次恢复还剩多少 tick（取模，若 elapsed 为负则视为 0）
                long mod = elapsedSinceLast >= 0 ? (elapsedSinceLast % RESTORE_INTERVAL_TICKS) : 0;
                long remainTicksToNext = RESTORE_INTERVAL_TICKS - mod;
                if (remainTicksToNext <= 0) remainTicksToNext = 0;
                long remainSeconds = (remainTicksToNext + 19) / 20; // 向上取整秒
                tooltip.add(Component.literal("装填时间: " + remainSeconds + "s")
                        .withStyle(ChatFormatting.WHITE));
            }
        }
    }

    /**
     * 获得当前耐久（如无则初始化为 MAX_DURABILITY）
     */
    private int getDurability(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_DURABILITY)) {
            tag.putInt(TAG_DURABILITY, MAX_DURABILITY);
            return MAX_DURABILITY;
        }
        return tag.getInt(TAG_DURABILITY);
    }

    private void setDurability(ItemStack stack, int v) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_DURABILITY, v);
    }

    /**
     * 检查是否可以射击（未越过射击内部冷却，且有弹药）
     * 注意：装填期间如果仍有弹药，允许射击（并且射击不会改变装填计时）
     */
    public boolean canShoot(ItemStack stack, Player player) {
        if (player == null) return false;
        CompoundTag tag = stack.getOrCreateTag();
        long currentTime = player.level().getGameTime();
        long lastShootTime = tag.getLong(TAG_SHOOT_TICK);
        if (currentTime < lastShootTime + SHOOT_COOLDOWN_TICKS) return false;

        int dur = getDurability(stack);
        return dur > 0;
    }

    /**
     * 触发一次射击（会消耗弹药）
     * 关键改动：只有在弹药降为 < MAX 并且尚未存在装填计时时，才开始装填计时。
     * 在装填进行中再次射击**不**会重置 TAG_COOLDOWN_START_TICK。
     */
    public void shoot(ItemStack stack, Player player, InteractionHand hand) {
        if (player == null) return;
        Level level = player.level();

        if (player.isSpectator()) return;

        if (!canShoot(stack, player)) return;

        CompoundTag tag = stack.getOrCreateTag();
        long currentTime = level.getGameTime();

        // 记录开火时刻 & 设置客户端后坐力进度
        tag.putLong(TAG_SHOOT_TICK, currentTime);
        tag.putFloat(TAG_SHOOT_PROGRESS, 0.0f);

        // 播放音效
        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SHULKER_CLOSE, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        // 服务端生成实体
        if (!level.isClientSide) {
            HealBullet arrow = new HealBullet(level, player);

            float velocity = 3.5F;
            float inaccuracy = 4.0F;
            if (isADSState(stack)) {
                inaccuracy = 0.1F;
            }

            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, inaccuracy);
            level.addFreshEntity(arrow);
        }

        player.getCooldowns().addCooldown(this, 10);

        // 消耗弹药
        int dur = getDurability(stack);
        dur = Math.max(0, dur - 1);
        setDurability(stack, dur);

        // 如果弹药现在少于最大值且尚未存在装填计时标记，则开始装填计时（只设一次）
        if (dur < MAX_DURABILITY && !tag.contains(TAG_COOLDOWN_START_TICK)) {
            tag.putLong(TAG_COOLDOWN_START_TICK, currentTime);
        }

        // 如果弹药变为 0，取消 ADS（但不重置计时）
        if (dur <= 0) {
            tag.putBoolean(TAG_ADS_STATE, false);
        }
    }

    /**
     * 右键切换瞄准状态（若弹药为0则不可切换）
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();
        long currentTime = level.getGameTime();
        long lastUseTime = tag.getLong(TAG_LAST_USE_TICK);

        if (currentTime < lastUseTime + ADS_COOLDOWN_TICKS) {
            return InteractionResultHolder.pass(stack);
        }

        int cur = tag.contains(TAG_DURABILITY) ? tag.getInt(TAG_DURABILITY) : MAX_DURABILITY;
        if (cur <= 0) {
            if (!level.isClientSide) {
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ITEM_BREAK,
                        SoundSource.PLAYERS,
                        0.6f, 0.9f);
            }
            return InteractionResultHolder.fail(stack);
        }

        tag.putLong(TAG_LAST_USE_TICK, currentTime);

        boolean newState = !tag.getBoolean(TAG_ADS_STATE);
        tag.putBoolean(TAG_ADS_STATE, newState);

        player.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.pass(stack);
    }

    public static boolean isADSState(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(TAG_ADS_STATE);
    }

    public static float getADSProgress(ItemStack stack, float partialTicks) {
        CompoundTag tag = stack.getOrCreateTag();
        float prev = tag.getFloat(TAG_ADS_PROGRESS_PREV);
        float now = tag.getFloat(TAG_ADS_PROGRESS);
        return prev + (now - prev) * partialTicks;
    }

    public static float getShootProgress(ItemStack stack, float partialTicks) {
        CompoundTag tag = stack.getOrCreateTag();
        float prev = tag.getFloat(TAG_SHOOT_PROGRESS_PREV);
        float now = tag.getFloat(TAG_SHOOT_PROGRESS);
        return prev + (now - prev) * partialTicks;
    }

    public static boolean isPlayerAiming(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (mainHand.getItem() instanceof HormoneGunItem && isADSState(mainHand)) {
            return true;
        }
        if (offHand.getItem() instanceof HormoneGunItem && isADSState(offHand)) {
            return true;
        }

        return false;
    }

    /**
     * 每帧物品 tick：
     * - 服务端：处理分步装填（每 RESTORE_INTERVAL_TICKS 恢复 1 发），直到弹药满时移除计时标记
     * - 客户端：动画平滑（保持原实现）
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!(entity instanceof Player player)) return;

        // 服务端：分步装填逻辑
        if (!level.isClientSide) {
            long current = level.getGameTime();

            // 如果存在装填计时标记，执行恢复
            if (tag.contains(TAG_COOLDOWN_START_TICK)) {
                long lastRestore = tag.getLong(TAG_COOLDOWN_START_TICK);
                int curDur = tag.contains(TAG_DURABILITY) ? tag.getInt(TAG_DURABILITY) : MAX_DURABILITY;

                if (curDur < MAX_DURABILITY) {
                    long elapsed = current - lastRestore;
                    int restores = (int) (elapsed / RESTORE_INTERVAL_TICKS);
                    if (restores > 0) {
                        int canRestore = Math.min(restores, MAX_DURABILITY - curDur);
                        curDur += canRestore;
                        setDurability(stack, curDur);

                        // 前移 lastRestore 时间点以反映已消耗的恢复段（保持计时连续，不被射击影响）
                        long newLastRestore = lastRestore + (long) canRestore * RESTORE_INTERVAL_TICKS;
                        tag.putLong(TAG_COOLDOWN_START_TICK, newLastRestore);

                        // 到满则移除计时标记
                        if (curDur >= MAX_DURABILITY) {
                            tag.remove(TAG_COOLDOWN_START_TICK);
                            level.playSound(null, player.blockPosition(),
                                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                                    SoundSource.PLAYERS,
                                    0.8f, 1.0f);
                        }
                    }
                } else {
                    // 已满但存在标记则清理
                    tag.remove(TAG_COOLDOWN_START_TICK);
                }
            } else {
                // 如果没有装填标记但弹药不足（例如读取存档后），马上启动装填计时（从当前刻开始）
                int curDur = tag.contains(TAG_DURABILITY) ? tag.getInt(TAG_DURABILITY) : MAX_DURABILITY;
                if (curDur < MAX_DURABILITY) {
                    tag.putLong(TAG_COOLDOWN_START_TICK, current);
                } else {
                    // 确保初始耐久存在
                    if (!tag.contains(TAG_DURABILITY)) tag.putInt(TAG_DURABILITY, MAX_DURABILITY);
                }
            }
        }

        // 客户端：动画与平滑逻辑（保持原样）
        if (!level.isClientSide) return;
        tag.putFloat(TAG_ADS_PROGRESS_PREV, tag.getFloat(TAG_ADS_PROGRESS));
        tag.putFloat(TAG_SHOOT_PROGRESS_PREV, tag.getFloat(TAG_SHOOT_PROGRESS));

        boolean main = ItemStack.isSameItem(stack, player.getMainHandItem());
        boolean off = ItemStack.isSameItem(stack, player.getOffhandItem());
        boolean isHoldingAndSelected = (main && selected) || off;

        if (isHoldingAndSelected) {
            boolean aiming = isADSState(stack);
            float target = aiming ? 1f : 0f;
            float currentVal = tag.getFloat(TAG_ADS_PROGRESS);

            if (currentVal < target) currentVal = Math.min(target, currentVal + ADS_STEP_PER_TICK);
            else currentVal = Math.max(target, currentVal - ADS_STEP_PER_TICK);

            tag.putFloat(TAG_ADS_PROGRESS, currentVal);
        } else {
            float currentVal = tag.getFloat(TAG_ADS_PROGRESS);
            if (currentVal > 0f) {
                currentVal = Math.max(0f, currentVal - ADS_STEP_PER_TICK * 2f);
                tag.putFloat(TAG_ADS_PROGRESS, currentVal);
            } else {
                tag.putFloat(TAG_ADS_PROGRESS, 0f);
                tag.putBoolean(TAG_ADS_STATE, false);
            }
        }

        float shoot = tag.getFloat(TAG_SHOOT_PROGRESS);
        if (shoot > 0.001f) {
            shoot *= SHOOT_DECAY_FACTOR;
            if (shoot < 0.001f) shoot = 0;
            tag.putFloat(TAG_SHOOT_PROGRESS, shoot);
        } else {
            tag.putFloat(TAG_SHOOT_PROGRESS, 0f);
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private final HumanoidModel.ArmPose GUN_HOLD_POSE =
                    HumanoidModel.ArmPose.create("GUN_HOLD", true, (model, entity, arm) -> {
                        if (arm == HumanoidArm.RIGHT) {
                            model.rightArm.xRot = (float)Math.toRadians(-10);
                            model.rightArm.yRot = (float)Math.toRadians(-5);
                            model.rightArm.zRot = (float)Math.toRadians(-2);
                        } else {
                            model.leftArm.xRot = (float)Math.toRadians(-8);
                            model.leftArm.yRot = (float)Math.toRadians(6);
                            model.leftArm.zRot = (float)Math.toRadians(2);
                        }
                    });

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return GUN_HOLD_POSE;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack item, float partialTick, float equip, float swing) {

                float ads = getADSProgress(item, partialTick);
                float side = arm == HumanoidArm.RIGHT ? 1f : -1f;
                float recoil = getShootProgress(item, partialTick);

                if (recoil < 0.0001f) {
                    swing = 0f;
                }

                float bob = 1f - ads * 0.75f;

                if (equip > 0.001f) {
                    float r = (float)Math.sqrt(equip);
                    poseStack.translate(0, -0.2f * r, -0.4f * equip);
                    poseStack.mulPose(Axis.XP.rotationDegrees(40f * r));
                    poseStack.mulPose(Axis.YP.rotationDegrees(side * -5f * r));
                }

                poseStack.translate(side * 0.12f, -0.28f, -0.38f);
                poseStack.mulPose(Axis.XP.rotationDegrees(-15));
                poseStack.mulPose(Axis.YP.rotationDegrees(side * 12));
                poseStack.mulPose(Axis.ZP.rotationDegrees(side * -2));
                poseStack.scale(0.6f, 0.6f, 0.6f);

                float s = ads * ads * (ads * (ads * 6f - 15f) + 10f);
                float blend = s * (1f - equip);

                poseStack.translate(-side * 0.12f * blend * bob, 0.3f * blend * bob, -0.6f * blend);
                poseStack.mulPose(Axis.YP.rotationDegrees(-side * 12f * blend));
                poseStack.mulPose(Axis.XP.rotationDegrees(12f * blend));

                if (recoil > 0.0001f) {
                    float rc = 1f - (float)Math.pow(1f - recoil, 3);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-3f * rc));
                }

                return true;
            }
        });
    }
}
