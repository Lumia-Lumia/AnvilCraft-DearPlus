package dev.anvilcraft.addon.dearpluscelestialreforge.init;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.block.ReforgingPanelBlock;
import dev.anvilcraft.lib.v2.registrum.providers.RegistrumRecipeProvider;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntry;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import static dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge.REGISTRUM;

public class AddonBlocks {
    static {
        REGISTRUM.defaultCreativeTab(AddonItemGroups.ADDON_ITEMS.getKey());
    }

    public static final BlockEntry<Block> AUTUMNIUM_ALLOY_BLOCK = REGISTRUM
        .block("autumnium_alloy_block", Block::new)
        .simpleItem()
        .recipe((ctx, prov) -> {
            // 9 Autumnium Alloy → 1 Autumnium Alloy Block
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ctx.getEntry())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', AddonItems.AUTUMNIUM_ALLOY.get())
                .unlockedBy("has_autumnium_alloy",
                    RegistrumRecipeProvider.has(AddonItems.AUTUMNIUM_ALLOY.get()))
                .save(prov);

            // 1 Autumnium Alloy Block → 9 Autumnium Alloy
            ShapelessRecipeBuilder.shapeless(
                    RecipeCategory.MISC,
                    AddonItems.AUTUMNIUM_ALLOY.get(),
                    9
                )
                .requires(ctx.getEntry())
                .unlockedBy("has_autumnium_alloy_block",
                    RegistrumRecipeProvider.has(ctx.getEntry()))
                .save(prov, AnvilCraftDearPlusCelestialReforge.of("autumnium_alloy_from_block"));

            // 1 铜锭 + 1 陶瓦 + 1 is_autumnium_component → 1 Autumnium Alloy
            ShapelessRecipeBuilder.shapeless(
                    RecipeCategory.MISC,
                    AddonItems.AUTUMNIUM_ALLOY.get(),
                    1
                )
                .requires(Items.COPPER_INGOT)
                .requires(Items.TERRACOTTA)
                .requires(ModTags.IS_AUTUMNIUM_COMPONENT)
                .unlockedBy("has_autumnium_component",
                    RegistrumRecipeProvider.has(ModTags.IS_AUTUMNIUM_COMPONENT))
                .save(prov, AnvilCraftDearPlusCelestialReforge.of("autumnium_alloy_from_components"));
        })
        .register();

    public static final BlockEntry<ReforgingPanelBlock> REFORGING_PANEL = REGISTRUM
        .block("reforging_panel", ReforgingPanelBlock::new)
        .blockstate((ctx, prov) -> {})
        .item()
            .model((ctx, prov) -> {})
            .build()
        .lang("Reforging Panel")
        .register();

    public static void register() {
    }
}
