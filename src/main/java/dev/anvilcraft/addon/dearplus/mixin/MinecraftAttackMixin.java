package dev.anvilcraft.addon.dearplus.mixin;

import dev.anvilcraft.addon.dearplus.util.TrueSweepHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 标记客户端是否正在「左键攻击」中，用于区分攻击挥刀与交互/丢物品的挥刀。
 */
@Mixin(Minecraft.class)
public abstract class MinecraftAttackMixin {
    @Inject(method = "startAttack", at = @At("HEAD"))
    private void attackStart(CallbackInfoReturnable<Boolean> cir) {
        Level level = Minecraft.getInstance().level;
        if (level != null) TrueSweepHelper.markClientAttacking(level.getGameTime());
    }

    @Inject(method = "startAttack", at = @At("RETURN"))
    private void attackEnd(CallbackInfoReturnable<Boolean> cir) {
        TrueSweepHelper.CLIENT_ATTACKING.remove();
    }
}
