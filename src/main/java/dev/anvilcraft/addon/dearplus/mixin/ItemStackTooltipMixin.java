package dev.anvilcraft.addon.dearplus.mixin;

import dev.anvilcraft.addon.dearplus.init.AddonComponents;
import dev.anvilcraft.addon.dearplus.item.property.component.AffixHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * 偏安词条：把被吸收（无效化）的附魔显示在 tooltip 最上面（所有词条之前）。
 */
@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {
    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void addTranquilEnchantmentsToTooltip(
        Item.TooltipContext tooltipContext,
        Player player,
        TooltipFlag tooltipFlag,
        CallbackInfoReturnable<List<Component>> cir
    ) {
        ItemStack self = (ItemStack) (Object) this;
        ItemEnchantments stored = AffixHelper.getTranquilEnchantments(self);
        if (stored.isEmpty()) return;

        // 先渲染到临时列表，再整体插入到名称下方第一行（词条之前）
        List<Component> lines = new ArrayList<>();
        self.addToTooltip(
            AddonComponents.TRANQUIL_ENCHANTMENTS,
            tooltipContext,
            tooltip -> lines.add(tooltip.copy().withColor(0x5D241F)),
            tooltipFlag
        );
        if (!lines.isEmpty()) {
            cir.getReturnValue().addAll(Math.min(1, cir.getReturnValue().size()), lines);
        }
    }
}
