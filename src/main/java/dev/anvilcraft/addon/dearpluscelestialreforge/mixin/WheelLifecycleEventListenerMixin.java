package dev.anvilcraft.addon.dearpluscelestialreforge.mixin;

import dev.anvilcraft.addon.dearpluscelestialreforge.item.AutumniumResonatorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 秋枫共振器：无法通过 Alt 打开形态切换轮盘（只有共振器形态，无需切换）。
 */
@Mixin(targets = "dev.dubhe.anvilcraft.client.event.WheelLifecycleEventListener")
public abstract class WheelLifecycleEventListenerMixin {
    @Inject(method = "openResonatorWheel", at = @At("HEAD"), cancellable = true)
    private static void preventAutumniumResonatorWheel(long gameTime, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof AutumniumResonatorItem)) {
            stack = player.getOffhandItem();
        }
        if (stack.getItem() instanceof AutumniumResonatorItem) {
            ci.cancel();
        }
    }
}
