package dev.anvilcraft.addon.dearplus.event;

import dev.anvilcraft.addon.dearplus.AnvilCraftDearPlus;
import dev.anvilcraft.addon.dearplus.item.property.component.AffixHelper;
import dev.anvilcraft.addon.dearplus.item.property.component.BladeAffixes;
import dev.anvilcraft.addon.dearplus.recipe.data.VyingMergeData;
import dev.anvilcraft.addon.dearplus.util.AffixNumberFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 物品 Tooltip 监听器：显示词条与物品描述。
 */
@EventBusSubscriber(modid = AnvilCraftDearPlus.MOD_ID)
public class AddonTooltipEventListener {
    public static final String AFFIX_TRANQUIL = "affix.anvilcraft_dearplus.tranquil";
    public static final String AFFIX_VYING = "affix.anvilcraft_dearplus.vying";
    public static final String AFFIX_CHOPPER = "affix.anvilcraft_dearplus.chopper";
    public static final String AFFIX_DECAPITATOR = "affix.anvilcraft_dearplus.decapitator";
    public static final String AFFIX_DISSPOSSESSOR = "affix.anvilcraft_dearplus.dispossessor";

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        // 词条：收集后插入到名称下方第一行（所有其他信息之前）
        // 显示格式参考铁砧工艺：词条名 + 冒号 + 描述，等级通过翻译参数传入
        List<Component> affixLines = new ArrayList<>();
        if (AffixHelper.hasTranquil(stack)) {
            affixLines.add(Component.translatable(AFFIX_TRANQUIL).withColor(0x79422C));
        }
        int vyingLevel = AffixHelper.getVyingLevel(stack);
        if (vyingLevel > 0) {
            affixLines.add(Component.translatable(AFFIX_VYING, AffixNumberFormat.format(vyingLevel, VyingMergeData.MAX_LEVEL)).withColor(0xFA9554));
        }
        BladeAffixes affixes = AffixHelper.getBladeAffixes(stack);
        if (affixes.chopper() > 0) {
            affixLines.add(
                Component.translatable(AFFIX_CHOPPER, AffixNumberFormat.format(affixes.chopper(), BladeAffixes.MAX_LEVEL))
                    .withColor(0xD7AF91)
            );
        }
        if (affixes.decapitator() > 0) {
            affixLines.add(
                Component.translatable(AFFIX_DECAPITATOR, AffixNumberFormat.format(affixes.decapitator(), BladeAffixes.MAX_LEVEL))
                    .withColor(0xB78766)
            );
        }
        if (affixes.dispossessor() > 0) {
            affixLines.add(
                Component.translatable(AFFIX_DISSPOSSESSOR, AffixNumberFormat.format(affixes.dispossessor(), BladeAffixes.MAX_LEVEL))
                    .withColor(0x965F3B)
            );
        }
        // 词条插入到名称下方（index 1），记录插入位置，使描述紧跟词条下方
        int insertIndex = Math.min(1, event.getToolTip().size());
        if (!affixLines.isEmpty()) {
            event.getToolTip().addAll(insertIndex, affixLines);
            insertIndex += affixLines.size();
        }

        // 描述：紧挨词条下方（无词条则在名称下方）
        String key = stack.getItem().builtInRegistryHolder().key().location().toString();
        if (!key.startsWith(AnvilCraftDearPlus.MOD_ID)) return;
        String descKey = stack.getDescriptionId() + ".desc";
        // 按换行符拆成多行 Component：不依赖渲染层对 \n 的换行处理，
        // 避免换行符被渲染成异常字符（tooltip 每行是独立组件，保证多行正确显示）
        String desc = Component.translatable(descKey).getString();
        for (String line : desc.split("\n", -1)) {
            event.getToolTip().add(insertIndex++, Component.literal(line).withStyle(ChatFormatting.GRAY));
        }
    }

    /**
     * 把「永恒」词条移到 tooltip 最顶部（名称下方第一行）。
     *
     * <p>AnvilCraft 的 propertyTooltip 会把词条插到附魔行之后（有附魔时被挤到最后），
     * 此处用 {@link EventPriority#LOWEST} 在其之后修正为置顶，确保永恒已添加到列表中。</p>
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void moveEternalToTop(ItemTooltipEvent event) {
        List<Component> tooltip = event.getToolTip();
        for (int i = 1; i < tooltip.size(); i++) {
            if (tooltip.get(i).getContents() instanceof TranslatableContents t
                && t.getKey().equals("tooltip.anvilcraft.property.eternal")) {
                Component eternal = tooltip.remove(i);
                tooltip.add(Math.min(1, tooltip.size()), eternal);
                return;
            }
        }
    }
}
