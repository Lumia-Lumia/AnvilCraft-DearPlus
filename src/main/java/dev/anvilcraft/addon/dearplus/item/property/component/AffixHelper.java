package dev.anvilcraft.addon.dearplus.item.property.component;

import dev.anvilcraft.addon.dearplus.init.AddonComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Iterator;

/**
 * 词条辅助工具类。
 *
 * <p>封装偏安/逐鹿/破竹/枭首/豪夺词条的常见判断与操作。</p>
 */
public final class AffixHelper {
    private AffixHelper() {
    }

    /** 是否拥有词条：偏安 */
    public static boolean hasTranquil(ItemStack stack) {
        return stack.has(AddonComponents.TRANQUIL);
    }

    /** 是否拥有词条：逐鹿 */
    public static boolean hasVying(ItemStack stack) {
        return getVyingLevel(stack) > 0;
    }

    /** 逐鹿等级（无逐鹿为 0） */
    public static int getVyingLevel(ItemStack stack) {
        return stack.getOrDefault(AddonComponents.VYING, 0);
    }

    /** 获取破竹/枭首/豪夺词条等级 */
    public static BladeAffixes getBladeAffixes(ItemStack stack) {
        return stack.getOrDefault(AddonComponents.BLADE_AFFIXES, BladeAffixes.EMPTY);
    }

    /** 设置破竹/枭首/豪夺词条等级 */
    public static void setBladeAffixes(ItemStack stack, BladeAffixes affixes) {
        if (affixes.isEmpty()) {
            stack.remove(AddonComponents.BLADE_AFFIXES);
        } else {
            stack.set(AddonComponents.BLADE_AFFIXES, affixes);
        }
    }

    /** 获取偏安物品被吸收（无效化）的附魔 */
    public static ItemEnchantments getTranquilEnchantments(ItemStack stack) {
        return stack.getOrDefault(AddonComponents.TRANQUIL_ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    /**
     * 偏安：把物品上已有的附魔转移到 {@link AddonComponents#TRANQUIL_ENCHANTMENTS}，
     * 使这些附魔全部失效（EnchantmentHelper 读不到），但保留在 tooltip 中显示。
     */
    public static void absorbEnchantments(ItemStack stack) {
        if (!hasTranquil(stack)) return;
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) return;

        ItemEnchantments stored = getTranquilEnchantments(stack);
        ItemEnchantments.Mutable storedMut = new ItemEnchantments.Mutable(stored);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            storedMut.set(enchantment, enchantments.getLevel(enchantment));
        }
        stack.set(AddonComponents.TRANQUIL_ENCHANTMENTS, storedMut.toImmutable());
        stack.remove(DataComponents.ENCHANTMENTS);
    }

    /**
     * 把偏安物品被吸收的附魔移回 {@link DataComponents#ENCHANTMENTS}。
     * 用于偏安物品升级为可附魔物品（如七环刀 → 九环刀）时恢复附魔。
     */
    public static void restoreEnchantments(ItemStack stack) {
        ItemEnchantments stored = getTranquilEnchantments(stack);
        if (stored.isEmpty()) return;
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(enchantments);
        for (Iterator<Holder<Enchantment>> it = stored.keySet().iterator(); it.hasNext(); ) {
            Holder<Enchantment> enchantment = it.next();
            mut.set(enchantment, stored.getLevel(enchantment));
        }
        stack.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
        stack.remove(AddonComponents.TRANQUIL_ENCHANTMENTS);
    }
}
