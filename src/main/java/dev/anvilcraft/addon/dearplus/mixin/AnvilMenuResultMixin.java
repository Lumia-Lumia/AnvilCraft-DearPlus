package dev.anvilcraft.addon.dearplus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.anvilcraft.addon.dearplus.item.property.component.AffixHelper;
import dev.dubhe.anvilcraft.util.anvil.AnvilMenuResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 逐鹿词条：铁砧工艺的铁砧变体（余烬/浮霜/皇家/超脱铁砧）附魔时不增加累计惩罚（REPAIR_COST）。
 *
 * <p>这些铁砧通过 {@link AnvilMenuResult#createResult} 计算结果，REPAIR_COST 由
 * {@code calculateFinalRepairCost} 返回（旧算法：{@code max(结果, 右侧)} 后调用
 * {@code calculateIncreasedRepairCost} 即 ×2+1）。对逐鹿物品返回未增加的 baseCost。</p>
 */
@Mixin(AnvilMenuResult.class)
public abstract class AnvilMenuResultMixin {
    @WrapOperation(
        method = "createResult",
        at = @At(value = "INVOKE", target = "Ldev/dubhe/anvilcraft/util/anvil/AnvilMenuResult;calculateFinalRepairCost(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;III)I")
    )
    private int vyingNoRepairCostIncrease(
        AnvilMenuResult instance,
        ItemStack inputRight,
        ItemStack result,
        int namingCost,
        int repairingCost,
        int price,
        Operation<Integer> original
    ) {
        if (AffixHelper.hasVying(result)) {
            // 逐鹿不累计惩罚：结果 REPAIR_COST 保持结果物品（逐鹿物品）自己的原值，
            // 不受右侧输入（附魔书等）的 repair_cost 影响。
            return Math.max(0, result.getOrDefault(DataComponents.REPAIR_COST, 0));
        }
        return original.call(instance, inputRight, result, namingCost, repairingCost, price);
    }
}
