package dev.anvilcraft.addon.dearpluscelestialreforge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.property.component.AffixHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 逐鹿词条：使物品在附魔台上附魔时无需达到等级要求。
 *
 * <p>经验扣除的免除由 {@link PlayerMixin} 处理（onEnchantmentPerformed 的 levels 置 0，
 * 同时保留魔咒种子刷新）。这里只负责放行 clickMenuButton 的等级检查。</p>
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
    @Shadow
    @Final
    private Container enchantSlots;

    /**
     * 把等级检查中读到的 {@link Abilities} 替换为 instabuild=true 的副本，
     * 使「!instabuild」为 false，从而免除等级要求。
     * 青金石检查走 {@code hasInfiniteMaterials()}（独立调用），不受影响。
     */
    @WrapOperation(
        method = "clickMenuButton",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAbilities()Lnet/minecraft/world/entity/player/Abilities;")
    )
    private Abilities vyingBypassLevelCost(Player player, Operation<Abilities> original) {
        if (AffixHelper.hasVying(this.enchantSlots.getItem(0))) {
            Abilities creative = new Abilities();
            creative.instabuild = true;
            return creative;
        }
        return original.call(player);
    }
}
