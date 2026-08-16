package dev.anvilcraft.addon.dearplus.init;

import dev.anvilcraft.addon.dearplus.AnvilCraftDearPlus;
import dev.anvilcraft.addon.dearplus.recipe.data.BladeAffixesData;
import dev.anvilcraft.addon.dearplus.recipe.data.DoubleBladeAffixesData;
import dev.anvilcraft.addon.dearplus.recipe.data.EnchantmentMaxMergeData;
import dev.anvilcraft.addon.dearplus.recipe.data.EternalMergeData;
import dev.anvilcraft.addon.dearplus.recipe.data.TranquilToEnchantmentsData;
import dev.anvilcraft.addon.dearplus.recipe.data.VyingMergeData;
import dev.dubhe.anvilcraft.api.recipe.data.ICustomDataComponent;
import dev.dubhe.anvilcraft.init.registry.ModRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 附属的自定义数据组件（用于 AnvilCraft 机器配方的结果组件计算）。
 */
public class ModCustomDataComponents {
    private static final DeferredRegister<ICustomDataComponent.Type<?>> DF = DeferredRegister.create(
        ModRegistries.CUSTOM_DATA_TYPE, AnvilCraftDearPlus.MOD_ID
    );

    public static final DeferredHolder<ICustomDataComponent.Type<?>, BladeAffixesData.Type> BLADE_AFFIXES =
        DF.register("blade_affixes", BladeAffixesData.Type::new);

    public static final DeferredHolder<ICustomDataComponent.Type<?>, TranquilToEnchantmentsData.Type>
        TRANQUIL_TO_ENCHANTMENTS =
        DF.register("tranquil_to_enchantments", TranquilToEnchantmentsData.Type::new);

    /** 两把九环刀合并：词条叠加（和，上限 255） */
    public static final DeferredHolder<ICustomDataComponent.Type<?>, DoubleBladeAffixesData.Type>
        DOUBLE_BLADE_AFFIXES =
        DF.register("double_blade_affixes", DoubleBladeAffixesData.Type::new);

    /** 两把九环刀合并：结果必带「永恒」词条 */
    public static final DeferredHolder<ICustomDataComponent.Type<?>, EternalMergeData.Type>
        ETERNAL_MERGE =
        DF.register("eternal_merge", EternalMergeData.Type::new);

    /** 两把九环刀合并：附魔取两把最大值（不考虑互斥） */
    public static final DeferredHolder<ICustomDataComponent.Type<?>, EnchantmentMaxMergeData.Type>
        ENCHANTMENT_MAX_MERGE =
        DF.register("enchantment_max_merge", EnchantmentMaxMergeData.Type::new);

    /** 两把九环刀合并：逐鹿等级（相同 +1、不同取最大、上限 10） */
    public static final DeferredHolder<ICustomDataComponent.Type<?>, VyingMergeData.Type>
        VYING_MERGE =
        DF.register("vying_merge", VyingMergeData.Type::new);

    public static void register(IEventBus bus) {
        DF.register(bus);
    }
}
