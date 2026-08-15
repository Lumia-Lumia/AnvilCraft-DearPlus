package dev.anvilcraft.addon.dearpluscelestialreforge.init;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.recipe.AutumniumHeatingRecipe;
import dev.anvilcraft.addon.dearpluscelestialreforge.recipe.RingedAutumniumBroadswordCraftingRecipe;
import dev.anvilcraft.addon.dearpluscelestialreforge.recipe.RingedAutumniumBroadswordRepairRecipe;
import dev.anvilcraft.addon.dearpluscelestialreforge.recipe.AutumniumStampingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 附属的自定义配方序列化器。
 */
public class ModRecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> DR = DeferredRegister.create(
        Registries.RECIPE_SERIALIZER, AnvilCraftDearPlusCelestialReforge.MOD_ID
    );

    /** 三环刀工作台合成（按金属粒动态设置词条） */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RingedAutumniumBroadswordCraftingRecipe>>
        AUTUMNIUM_RING_BLADE_CRAFTING =
        DR.register("ringed_autumnium_broadsword_crafting", RingedAutumniumBroadswordCraftingRecipe.Serializer::new);

    /** 大环刀工作台修复（保留词条） */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RingedAutumniumBroadswordRepairRecipe>>
        AUTUMNIUM_RING_BLADE_REPAIR =
        DR.register("ringed_autumnium_broadsword_repair", RingedAutumniumBroadswordRepairRecipe.Serializer::new);

    /** 冲压升级（三环→五环） */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AutumniumStampingRecipe>>
        AUTUMNIUM_STAMPING =
        DR.register("autumnium_stamping", AutumniumStampingRecipe.Serializer::new);

    /** 高温熔炼升级（五环→七环） */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AutumniumHeatingRecipe>>
        AUTUMNIUM_HEATING =
        DR.register("autumnium_heating", AutumniumHeatingRecipe.Serializer::new);

    public static void register(IEventBus bus) {
        DR.register(bus);
    }
}
