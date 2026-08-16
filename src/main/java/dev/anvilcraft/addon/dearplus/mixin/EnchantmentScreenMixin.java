package dev.anvilcraft.addon.dearplus.mixin;

import dev.anvilcraft.addon.dearplus.item.property.component.AffixHelper;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 逐鹿词条（客户端）：附魔台按钮亮起只与青金石数量相关，免除等级要求。
 *
 * <p>原版 renderBg 中按钮不亮起的条件是
 * {@code ((青金石不足 || 等级不足) && !创造) || 无魔咒}。
 * 用 {@link Redirect} 把逐鹿物品的 {@code experienceLevel} 读取（字节码中 owner 为
 * {@link LocalPlayer}，因为 {@code Minecraft.player} 的静态类型是 LocalPlayer）改写为
 * MAX_VALUE，使「等级不足」判断恒为 false，按钮在青金石足够时即亮起。</p>
 */
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin {
    @Redirect(
        method = "renderBg",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I")
    )
    private int vyingLitButtons(LocalPlayer player) {
        EnchantmentMenu menu = ((MenuAccess<EnchantmentMenu>) (Object) this).getMenu();
        if (AffixHelper.hasVying(menu.getSlot(0).getItem())) {
            return Integer.MAX_VALUE;
        }
        return player.experienceLevel;
    }
}
