package dev.anvilcraft.addon.dearpluscelestialreforge.init;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.recipe.AutumniumAffixMergeOutcome;
import dev.anvilcraft.lib.v2.recipe.init.LibRegistries;
import dev.anvilcraft.lib.v2.recipe.outcome.IRecipeOutcome;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 附属的自定义世界内配方结果（outcome）类型注册表。
 *
 * <p>注册到 anvillib 的 {@link LibRegistries#OUTCOME_TYPE_REGISTRY}，使
 * {@link AutumniumAffixMergeOutcome} 的类型可被注册表枚举/序列化框架识别，
 * 避免 {@code getId()} 返回 null 导致 NPE。该结果仅由代码构造、不经 JSON 序列化，
 * 此处为注册表占位。</p>
 */
public final class AddonOutcomeTypes {
    private AddonOutcomeTypes() {
    }

    private static final DeferredRegister<IRecipeOutcome.Type<?>> DR = DeferredRegister.create(
        LibRegistries.OUTCOME_TYPE_REGISTRY, AnvilCraftDearPlusCelestialReforge.MOD_ID
    );

    /** 秋枫大环刀升级的词条合并结果 */
    public static final DeferredHolder<IRecipeOutcome.Type<?>, AutumniumAffixMergeOutcome.Type> AFFIX_MERGE =
        DR.register("autumnium_affix_merge", AutumniumAffixMergeOutcome.Type::new);

    public static void register(IEventBus modEventBus) {
        DR.register(modEventBus);
    }
}
