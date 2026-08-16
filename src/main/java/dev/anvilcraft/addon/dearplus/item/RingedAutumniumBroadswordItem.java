package dev.anvilcraft.addon.dearplus.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

/**
 * 秋枫大环刀（三环/五环/七环/九环）。
 *
 * <p>持有破竹/枭首/豪夺词条（由合成/升级时使用的金属粒决定），三/五/七环刀持「偏安」，
 * 九环刀持「逐鹿」。拥有真实横扫：即使攻击未命中目标也触发横扫之刃效果。</p>
 */
public class RingedAutumniumBroadswordItem extends SwordItem {
    public RingedAutumniumBroadswordItem(
        Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties
    ) {
        super(tier, properties.attributes(
            SwordItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier)
        ));
    }
}
