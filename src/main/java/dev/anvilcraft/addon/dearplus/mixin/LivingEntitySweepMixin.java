package dev.anvilcraft.addon.dearplus.mixin;

import dev.anvilcraft.addon.dearplus.item.RingedAutumniumBroadswordItem;
import dev.anvilcraft.addon.dearplus.util.TrueSweepHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 真实横扫：秋枫大环刀攻击挥空（点击空气）时也触发横扫之刃效果，伤害与直击相同。
 * 客户端攻击挥空的通知已由 {@code AddonAffixEvents.onAttackAir}（PlayerInteractEvent.LeftClickEmpty）发包完成，
 * 此处只负责服务端执行挥空横扫伤害。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySweepMixin {
    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"))
    private void trueSweepOnSwing(InteractionHand hand, CallbackInfo ci) {
        if (!((Object) this instanceof Player self)) return;
        if (TrueSweepHelper.isAttacking(self)) return; // 正在攻击目标，由 attack 处理
        if (self.level().isClientSide) return; // 只服务端计算伤害
        if (!TrueSweepHelper.isAttackSwing(self)) return; // 只在攻击挥空时横扫，排除交互/丢物品
        ItemStack weapon = self.getMainHandItem();
        if (!(weapon.getItem() instanceof RingedAutumniumBroadswordItem)) return;
        if (self.getAttackStrengthScale(0.5F) < 0.9F) return; // 需满蓄力

        DamageSource source = self.damageSources().playerAttack(self);
        float damage = (float) self.getAttributeValue(Attributes.ATTACK_DAMAGE);
        // 挥空横扫范围为面前区域（以玩家面前约 1.5 格处为中心，高度随视线俯仰变化）
        Vec3 look = self.getLookAngle();
        Vec3 center = self.getEyePosition().add(look.x * 1.5, look.y * 1.5, look.z * 1.5);
        TrueSweepHelper.sweepAround(
            self, new AABB(center, center).inflate(2.5, 1.0, 2.5), source, damage, null
        );
    }
}
