package dev.anvilcraft.addon.dearpluscelestialreforge.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 秋枫大环刀真实横扫的共享工具。
 */
public final class TrueSweepHelper {
    /**
     * 服务端：标记当前是否处于 Player.attack 中（存储 gameTime，同 tick 有效）。
     * 存时间戳而非布尔：即使异常中断未复位，下个 tick 旧值自动失效，避免永久卡死。
     */
    public static final ThreadLocal<Long> ATTACKING = new ThreadLocal<>();

    /** 客户端：标记当前是否在 Minecraft.startAttack（左键攻击）中（存储 gameTime，同 tick 有效） */
    public static final ThreadLocal<Long> CLIENT_ATTACKING = new ThreadLocal<>();

    /** 服务端：标记本次攻击（同 tick 有效） */
    public static void markAttacking(Player player) {
        ATTACKING.set(player.level().getGameTime());
    }

    /** 服务端：当前是否正处于 Player.attack 调用中（与标记同 tick） */
    public static boolean isAttacking(Player player) {
        Long tick = ATTACKING.get();
        return tick != null && tick == player.level().getGameTime();
    }

    /** 客户端：标记本次左键攻击（同 tick 有效） */
    public static void markClientAttacking(long gameTime) {
        CLIENT_ATTACKING.set(gameTime);
    }

    /** 客户端：当前是否正处于左键攻击中（与标记同 tick） */
    public static boolean isClientAttacking(long gameTime) {
        Long tick = CLIENT_ATTACKING.get();
        return tick != null && tick == gameTime;
    }

    /** 服务端：记录玩家最近一次「攻击挥刀」（本 tick 有效），用于排除交互/丢物品的挥刀 */
    private static final Map<UUID, Long> ATTACK_SWING_TICKS = new HashMap<>();

    /** 服务端：标记玩家本次挥刀来自攻击挥空 */
    public static void markAttackSwing(Player player) {
        long now = player.level().getGameTime();
        ATTACK_SWING_TICKS.put(player.getUUID(), now);
        // 清理所有已过期（>1 tick）的标记，Map 只保留活跃挥刀，防止只增不删
        ATTACK_SWING_TICKS.entrySet().removeIf(e -> now - e.getValue() > 1);
    }

    /** 服务端：判断本次挥刀是否为攻击挥空（近几个 tick 内标记过，容忍网络往返延迟） */
    public static boolean isAttackSwing(Player player) {
        Long tick = ATTACK_SWING_TICKS.get(player.getUUID());
        return tick != null && player.level().getGameTime() - tick <= 3;
    }

    private TrueSweepHelper() {
    }

    /**
     * 对玩家周围实体造成横扫伤害。真实横扫伤害 = 直击伤害 × √(横扫之刃等级 + 1)，向下取整；
     * 无横扫之刃时保持直击伤害不变。
     */
    public static void sweepAround(Player self, AABB area, DamageSource source, float damage, Entity exclude) {
        Holder<Enchantment> sweepingEdge = self.level().registryAccess()
            .registryOrThrow(Registries.ENCHANTMENT)
            .getHolderOrThrow(Enchantments.SWEEPING_EDGE);
        int sweepLevel = self.getMainHandItem().getEnchantmentLevel(sweepingEdge);
        if (sweepLevel > 0) {
            damage = (float) Math.floor(damage * Math.sqrt(sweepLevel + 1));
        }
        for (LivingEntity living : self.level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (living != self
                && living != exclude
                && !self.isAlliedTo(living)
                && (!(living instanceof ArmorStand) || !((ArmorStand) living).isMarker())
            ) {
                living.knockback(
                    0.4F,
                    Mth.sin(self.getYRot() * (float) (Math.PI / 180.0)),
                    -Mth.cos(self.getYRot() * (float) (Math.PI / 180.0))
                );
                living.hurt(source, damage);
                if (self.level() instanceof ServerLevel serverLevel) {
                    EnchantmentHelper.doPostAttackEffects(serverLevel, living, source);
                }
            }
        }
        self.level().playSound(
            null, self.getX(), self.getY(), self.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, self.getSoundSource(), 1.0F, 1.0F
        );
        self.sweepAttack();
    }
}
