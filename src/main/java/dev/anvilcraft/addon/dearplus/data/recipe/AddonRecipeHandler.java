package dev.anvilcraft.addon.dearplus.data.recipe;

import dev.anvilcraft.addon.dearplus.init.AddonComponents;
import dev.anvilcraft.addon.dearplus.init.AddonItems;
import dev.anvilcraft.addon.dearplus.init.ModTags;
import dev.anvilcraft.addon.dearplus.recipe.data.BladeAffixesData;
import dev.anvilcraft.addon.dearplus.recipe.data.DoubleBladeAffixesData;
import dev.anvilcraft.addon.dearplus.recipe.data.EnchantmentMaxMergeData;
import dev.anvilcraft.addon.dearplus.recipe.data.EternalMergeData;
import dev.anvilcraft.addon.dearplus.recipe.data.TranquilToEnchantmentsData;
import dev.anvilcraft.addon.dearplus.recipe.data.VyingMergeData;
import dev.anvilcraft.lib.v2.registrum.providers.RegistrumRecipeProvider;
import dev.dubhe.anvilcraft.api.recipe.data.NormalDataComponent;
import dev.dubhe.anvilcraft.api.recipe.slot.RecipeInputSlot;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.recipe.multiple.TwoToOneSmithingRecipe;

/**
 * 附属的机器配方数据生成器。
 */
public class AddonRecipeHandler {
    private AddonRecipeHandler() {
    }

    public static void init(RegistrumRecipeProvider provider) {
        // 二合一锻造：二合一模板 + 七环刀（材料槽）+ 两个金属粒（两个输入槽）→ 九环刀
        // 结果保留七环刀原有词条并累加两个金属粒的词条
        TwoToOneSmithingRecipe.builder()
            .material(AddonItems.AUTUMNIUM_RING_BLADE_7.get())
            .input(ModTags.IS_BROADSWORD_RING_COMPONENT)
            .input(ModTags.IS_BROADSWORD_RING_COMPONENT)
            .resultMerge(
                AddonItems.AUTUMNIUM_RING_BLADE_9.get(),
                NormalDataComponent.of(RecipeInputSlot.MATERIAL, AddonComponents.BLADE_AFFIXES),
                BladeAffixesData.of(0),
                BladeAffixesData.of(1),
                // 九环刀不再持有「偏安」：把七环刀被吸收的附魔移回 ENCHANTMENTS
                TranquilToEnchantmentsData.of(RecipeInputSlot.MATERIAL)
            )
            .save(provider);

        // 九环刀叠加：材料槽 = 超限多相合金，输入槽 = 两把九环刀 → 新九环刀
        // 破竹/枭首/豪夺取两把之和（上限 255）；结果必带「永恒」；附魔取两把最大值（不考虑互斥）；
        // 逐鹿等级：两把相同则 +1、不同则取最大（上限 10）
        TwoToOneSmithingRecipe.builder()
            .material(ModItems.MULTIPHASE_TRANSCENDIUM.get())
            .input(AddonItems.AUTUMNIUM_RING_BLADE_9.get())
            .input(AddonItems.AUTUMNIUM_RING_BLADE_9.get())
            .resultMerge(
                AddonItems.AUTUMNIUM_RING_BLADE_9.get(),
                DoubleBladeAffixesData.of(),
                EternalMergeData.of(),
                EnchantmentMaxMergeData.of(),
                VyingMergeData.of()
            )
            .save(provider, "ringed_autumnium_broadsword_9_merge");
    }
}
