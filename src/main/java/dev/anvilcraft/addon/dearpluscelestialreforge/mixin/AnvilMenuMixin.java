package dev.anvilcraft.addon.dearpluscelestialreforge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.AddonComponents;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.AddonItems;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.property.component.AffixHelper;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.property.component.BladeAffixes;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * 逐鹿词条：使物品在铁砧上附魔/操作时无需消耗经验等级（费用显示为 0）。
 * 秋枫九环刀：两把九环刀可在铁砧中合并，词条等级取两者最小值，魔咒按原版规则合并，费用为 0。
 */
@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow
    @Final
    private DataSlot cost;

    @Shadow
    public int repairItemCountCost;

    protected AnvilMenuMixin(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(null, containerId, playerInventory, access);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void handleNineRingBladeMerge(CallbackInfo ci) {
        ItemStack left = this.inputSlots.getItem(0);
        ItemStack right = this.inputSlots.getItem(1);
        // 偏安：无法在铁砧中进行任何操作（附魔/修复/合并）
        if (AffixHelper.hasTranquil(left) || AffixHelper.hasTranquil(right)) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            ci.cancel();
            return;
        }
        if (right.isEmpty()
            || !left.is(right.getItem())
            || !left.is(AddonItems.AUTUMNIUM_RING_BLADE_9.get())
            || !right.is(AddonItems.AUTUMNIUM_RING_BLADE_9.get())
        ) {
            return;
        }

        // 两把九环刀合并：词条取最小值；魔咒按原版规则合并；费用为 0（逐鹿免经验）
        ItemStack result = left.copy();

        // 词条：破竹/枭首/豪夺等级 = 两者的最小值
        BladeAffixes la = AffixHelper.getBladeAffixes(left);
        BladeAffixes ra = AffixHelper.getBladeAffixes(right);
        BladeAffixes merged = new BladeAffixes(
            Math.min(la.chopper(), ra.chopper()),
            Math.min(la.decapitator(), ra.decapitator()),
            Math.min(la.dispossessor(), ra.dispossessor())
        );
        AffixHelper.setBladeAffixes(result, merged);

        // 魔咒合并（参考原版铁砧：同魔咒等级相等则 +1，否则取大，并受等级上限约束；不兼容的跳过）
        ItemEnchantments leftEnch = EnchantmentHelper.getEnchantmentsForCrafting(left);
        ItemEnchantments rightEnch = EnchantmentHelper.getEnchantmentsForCrafting(right);
        ItemEnchantments.Mutable mergedEnch = new ItemEnchantments.Mutable(leftEnch);
        for (Map.Entry<Holder<Enchantment>, Integer> entry : rightEnch.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            Enchantment enchantment = holder.value();
            if (!enchantment.canEnchant(result)) continue;
            // 与已合并魔咒的兼容性检查
            boolean compatible = true;
            for (Holder<Enchantment> existing : mergedEnch.keySet()) {
                if (!existing.equals(holder) && !Enchantment.areCompatible(holder, existing)) {
                    compatible = false;
                    break;
                }
            }
            if (!compatible) continue;
            int existingLevel = mergedEnch.getLevel(holder);
            int level = entry.getValue();
            level = existingLevel == level ? level + 1 : Math.max(level, existingLevel);
            level = Math.min(level, enchantment.getMaxLevel());
            mergedEnch.set(holder, level);
        }
        result.set(DataComponents.ENCHANTMENTS, mergedEnch.toImmutable());

        // 满耐久
        result.setDamageValue(0);
        // 九环刀持逐鹿，铁砧合并后 REPAIR_COST 清零
        result.set(DataComponents.REPAIR_COST, 0);

        // 逐鹿等级：两把相同则 +1、不同则取最大（上限 10），与二合一锻造规则一致
        int vyingLeft = AffixHelper.getVyingLevel(left);
        int vyingRight = AffixHelper.getVyingLevel(right);
        int vyingMerged;
        if (vyingLeft == 0 && vyingRight == 0) {
            vyingMerged = 0;
        } else if (vyingLeft == vyingRight) {
            vyingMerged = Math.min(10, vyingLeft + 1);
        } else {
            vyingMerged = Math.max(vyingLeft, vyingRight);
        }
        if (vyingMerged > 0) {
            result.set(AddonComponents.VYING, vyingMerged);
        }

        this.resultSlots.setItem(0, result);
        this.repairItemCountCost = 0;
        this.cost.set(0);
        ci.cancel();
    }

    /**
     * 逐鹿：铁砧附魔时经验费用显示为 0（把所有 cost.set 改写为 0，从而「已太昂贵」的 40 级上限也不会触发）。
     */
    @ModifyArg(
        method = "createResult",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V"),
        index = 0
    )
    private int vyingZeroAnvilCost(int value) {
        if (AffixHelper.hasVying(this.inputSlots.getItem(0))) {
            return 0;
        }
        return value;
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void vyingBypassLevelCost(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
        ItemStack left = this.inputSlots.getItem(0);
        // 逐鹿：cost 已被置 0，只要结果槽有物品即可拾取（免经验）
        if (AffixHelper.hasVying(left) && hasStack) {
            cir.setReturnValue(true);
        }
    }

    @WrapOperation(
        method = "onTake",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V")
    )
    private void vyingFreeTake(Player player, int levels, Operation<Void> original) {
        if (!AffixHelper.hasVying(this.inputSlots.getItem(0))) {
            original.call(player, levels);
        }
    }

    /**
     * 逐鹿：铁砧费用显示为 0（根治）。
     *
     * <p>{@code getCost()} 是所有铁砧变体（原版 + 铁砧工艺的余烬/浮霜/皇家/超脱铁砧，均继承
     * {@link AnvilMenu}）客户端屏幕读取费用的统一入口，在此归零即可一次性覆盖所有铁砧的显示，
     * 无需逐个铁砧加 mixin。经验扣除已由 {@link #vyingFreeTake} 免除。</p>
     */
    @Inject(method = "getCost", at = @At("HEAD"), cancellable = true)
    private void vyingZeroDisplayCost(CallbackInfoReturnable<Integer> cir) {
        if (AffixHelper.hasVying(this.inputSlots.getItem(0))) {
            cir.setReturnValue(0);
        }
    }

    /**
     * 逐鹿：铁砧附魔时不增加累计惩罚（REPAIR_COST）。
     *
     * <p>原版 createResult 末尾会把结果 REPAIR_COST 改为 {@code calculateIncreasedRepairCost(max(左,右))}（即 ×2+1）。
     * 对逐鹿物品跳过这次增加，使累计惩罚保持不变。</p>
     */
    @WrapOperation(
        method = "createResult",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;calculateIncreasedRepairCost(I)I")
    )
    private int vyingNoRepairCostIncrease(int repairCost, Operation<Integer> original) {
        if (AffixHelper.hasVying(this.inputSlots.getItem(0))) {
            // 逐鹿不累计惩罚：结果 REPAIR_COST 保持左物品（逐鹿物品）自己的原值，
            // 不受右侧输入（附魔书等）的 repair_cost 影响。
            return Math.max(0, this.inputSlots.getItem(0).getOrDefault(DataComponents.REPAIR_COST, 0));
        }
        return original.call(repairCost);
    }
}
