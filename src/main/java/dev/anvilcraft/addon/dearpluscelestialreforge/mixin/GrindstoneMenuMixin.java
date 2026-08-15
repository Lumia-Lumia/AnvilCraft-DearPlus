package dev.anvilcraft.addon.dearpluscelestialreforge.mixin;

import dev.anvilcraft.addon.dearpluscelestialreforge.item.RingedAutumniumBroadswordItem;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.property.component.AffixHelper;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 偏安词条：使物品无法被砂轮祛魔，也无法在砂轮中通过合成修复。
 * 秋枫大环刀：无法通过两个相同刀在砂轮中合成修复（祛魔不受影响）。
 */
@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin {
    @Inject(method = "computeResult", at = @At("HEAD"), cancellable = true)
    private void tranquilBlockGrindstone(ItemStack first, ItemStack second, CallbackInfoReturnable<ItemStack> cir) {
        // 偏安物品（含三/五/七环刀）：无法祛魔/合成修复
        if (AffixHelper.hasTranquil(first) || AffixHelper.hasTranquil(second)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        // 两个相同大环刀：阻止砂轮合成修复（单把祛魔不受影响，九环刀可祛魔）
        if (first.getItem() instanceof RingedAutumniumBroadswordItem
            && second.getItem() instanceof RingedAutumniumBroadswordItem) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
