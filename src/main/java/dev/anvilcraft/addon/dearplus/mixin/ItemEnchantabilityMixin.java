package dev.anvilcraft.addon.dearplus.mixin;

import dev.anvilcraft.addon.dearplus.item.property.component.AffixHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 偏安词条：使物品无法通过附魔台/铁砧等正常途径附魔。
 */
@Mixin(Item.class)
public abstract class ItemEnchantabilityMixin {
    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void tranquilBlockEnchantable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (AffixHelper.hasTranquil(stack)) {
            cir.setReturnValue(false);
        }
    }
}
