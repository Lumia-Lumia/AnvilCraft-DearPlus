package dev.anvilcraft.addon.dearpluscelestialreforge.item;

import com.mojang.datafixers.util.Unit;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.AddonComponents;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.AddonTiers;
import dev.dubhe.anvilcraft.item.ResonatorItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * 秋枫共振器。
 *
 * <p>持有词条「偏安」。仅有共振器形态（不可切换为具体工具形态），可共振挖掘，
 * 无法在铁砧中修复，无法附魔，无法被砂轮祛魔。</p>
 */
public class AutumniumResonatorItem extends ResonatorItem {
    /**
     * 基础攻击伤害（共振器形态）。
     * ResonatorItem.createAttributes 实际攻击 = 1 + attackDamage + tierBonus（tier=2），
     * 故 3 → 实际 6。
     */
    private static final float BASE_ATTACK_DAMAGE = 3;

    public AutumniumResonatorItem(Properties properties) {
        super(
            AddonTiers.AUTUMNIUM,
            properties
                .attributes(ResonatorItem.createAttributes(AddonTiers.AUTUMNIUM, BASE_ATTACK_DAMAGE, -2.8f))
                .component(AddonComponents.TRANQUIL, Unit.INSTANCE)
        );
    }

    @Override
    protected double getBaseAttackDamage() {
        return BASE_ATTACK_DAMAGE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // 秋枫共振器无法切换工具类型，跳过 ResonatorItem 的通用「切换模式」提示。
        // 物品描述由 AddonTooltipEventListener 基于 .desc 翻译键统一添加。
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        // 挖掘等级铁：需匹配对应工具类型规则（斧/锄/镐/锹），且方块不超出铁等级。
        // ResonatorItem 未覆写此方法，默认仅按 Tool 组件判断，会误把黑曜石/远古残骸等判为可收获。
        Tool tool = stack.get(DataComponents.TOOL);
        if (tool == null || !tool.isCorrectForDrops(state)) {
            return false;
        }
        return !state.is(this.getTier().getIncorrectBlocksForDrops());
    }
}
