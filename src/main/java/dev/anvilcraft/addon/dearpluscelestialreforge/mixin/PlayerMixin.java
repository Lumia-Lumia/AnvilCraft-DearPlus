package dev.anvilcraft.addon.dearpluscelestialreforge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.RingedAutumniumBroadswordItem;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.property.component.AffixHelper;
import dev.anvilcraft.addon.dearpluscelestialreforge.util.TrueSweepHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 逐鹿词条：附魔台附魔时免除经验等级消耗。
 * 真实横扫：秋枫大环刀攻击时（无论命中与否）触发大范围横扫，横扫伤害与直击相同。
 */
@Mixin(Player.class)
public abstract class PlayerMixin {
    @Shadow
    protected int enchantmentSeed;

    /**
     * 逐鹿：附魔台附魔不扣经验。取消原版扣除，但保留 {@code enchantmentSeed} 刷新，
     * 否则附魔台魔咒不会随机会话（再次附魔仍是同样的魔咒）。
     */
    @Inject(method = "onEnchantmentPerformed", at = @At("HEAD"), cancellable = true)
    private void vyingFreeEnchantCost(ItemStack stack, int levels, CallbackInfo ci) {
        if (AffixHelper.hasVying(stack)) {
            this.enchantmentSeed = ((Player) (Object) this).getRandom().nextInt();
            ci.cancel();
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void trueSweepAttackStart(Entity target, CallbackInfo ci) {
        TrueSweepHelper.markAttacking((Player) (Object) this);
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void trueSweepAttackEnd(Entity target, CallbackInfo ci) {
        TrueSweepHelper.ATTACKING.remove();
    }

    /**
     * 攻击目标时（无论命中与否）触发大范围横扫，伤害与直击相同。
     * 不受原版横扫 flag2 条件（疾跑/站地/移动速度）限制，与挥空横扫一致。
     */
    @WrapOperation(
        method = "attack",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    )
    private boolean trueSweepOnAttack(
        Entity target,
        DamageSource source,
        float amount,
        Operation<Boolean> original,
        @Local(ordinal = 0) ItemStack weapon,
        @Local(ordinal = 3) float f3
    ) {
        boolean hit = original.call(target, source, amount);
        if (weapon.getItem() instanceof RingedAutumniumBroadswordItem) {
            TrueSweepHelper.sweepAround(
                (Player) (Object) this, target.getBoundingBox().inflate(2.5, 1.0, 2.5), source, f3, target
            );
        }
        return hit;
    }

    /**
     * 秋枫大环刀禁用原版横扫（由 TrueSweepHelper 以更大范围替代），非刀保持原版。
     */
    @WrapOperation(
        method = "attack",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;")
    )
    private List<LivingEntity> skipVanillaSweep(
        Level level, Class<? extends LivingEntity> cls, AABB area, Operation<List<LivingEntity>> original
    ) {
        if (((Player) (Object) this).getMainHandItem().getItem() instanceof RingedAutumniumBroadswordItem) {
            return List.of();
        }
        return original.call(level, cls, area);
    }

    /**
     * 秋枫大环刀禁用原版横扫的音效与粒子（由 TrueSweepHelper 播放），非刀保持原版。
     */
    @WrapOperation(
        method = "attack",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;sweepAttack()V")
    )
    private void skipVanillaSweepEffects(Player instance, Operation<Void> original) {
        if (!(instance.getMainHandItem().getItem() instanceof RingedAutumniumBroadswordItem)) {
            original.call();
        }
    }
}
