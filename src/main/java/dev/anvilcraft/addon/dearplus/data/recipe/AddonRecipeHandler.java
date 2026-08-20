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
import dev.dubhe.anvilcraft.block.entity.celestial.LiquidCoverage;
import dev.dubhe.anvilcraft.block.entity.celestial.SpecialCelestialBodyRecipe;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.recipe.multiple.TwoToOneSmithingRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

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

        // 锻星砧特殊天体（隐藏天体）：砧子参数 + 种子物品匹配则发现
        initSpecialCelestialBodies(provider);
    }

    /// === 锻星砧特殊天体（隐藏天体） ===
    /// 参考 AnvilCraft SpecialCelestialBodyRecipeLoader：
    /// 玩家在种子格放入种子物品搜索时，砧子参数与种子物品（每个世界由 seedItems 伪随机选 1 个）都匹配则发现隐藏天体；
    /// needsCustomModel=true 走自定义模型渲染（不使用色板变色）。

    private static void initSpecialCelestialBodies(RegistrumRecipeProvider provider) {
        // 繁花星球
        saveSpecialCelestialBody(provider, "flower_planet", new SpecialCelestialBodyRecipe(
            "flower_planet",
            "anvilcraft_dearplus:block/celestial_body/planet_flower",
            true,
            32, 14, 20, 16,
            true, Optional.of(LiquidCoverage.MEDIUM),
            2, 2, 0f,
            List.of(mc("dandelion"), mc("poppy"), mc("blue_orchid"), mc("allium"), mc("azure_bluet"),
                mc("red_tulip"), mc("orange_tulip"), mc("white_tulip"), mc("pink_tulip"), mc("oxeye_daisy"),
                mc("cornflower"), mc("lily_of_the_valley"), mc("sunflower"), mc("lilac"), mc("rose_bush"), mc("peony")),
            List.of(item("minecraft:white_dye", 7), item("minecraft:red_dye", 7), item("minecraft:yellow_dye", 7),
                item("minecraft:blue_dye", 7), item("minecraft:light_gray_dye", 6), item("minecraft:gray_dye", 6),
                item("minecraft:black_dye", 6), item("minecraft:brown_dye", 6), item("minecraft:orange_dye", 6),
                item("minecraft:lime_dye", 6), item("minecraft:green_dye", 6), item("minecraft:cyan_dye", 6),
                item("minecraft:light_blue_dye", 6), item("minecraft:purple_dye", 6), item("minecraft:magenta_dye", 6),
                item("minecraft:pink_dye", 6)),
            List.of(item("minecraft:water", 100)),
            List.of(item("minecraft:dandelion", 1), item("minecraft:poppy", 1), item("minecraft:blue_orchid", 1),
                item("minecraft:allium", 1), item("minecraft:azure_bluet", 1), item("minecraft:red_tulip", 1),
                item("minecraft:orange_tulip", 1), item("minecraft:white_tulip", 1), item("minecraft:pink_tulip", 1),
                item("minecraft:oxeye_daisy", 1), item("minecraft:cornflower", 1), item("minecraft:lily_of_the_valley", 1),
                item("minecraft:wither_rose", 1), item("minecraft:torchflower", 1), item("minecraft:sunflower", 1),
                item("minecraft:lilac", 1), item("minecraft:rose_bush", 1), item("minecraft:peony", 1),
                item("minecraft:pitcher_plant", 1), item("minecraft:spore_blossom", 1), item("minecraft:flowering_azalea", 1),
                item("minecraft:pink_petals", 1), item("minecraft:honeycomb", 1), item("minecraft:tall_grass", 1),
                item("minecraft:large_fern", 1), item("minecraft:fern", 1), item("minecraft:small_dripleaf", 1),
                item("minecraft:glow_lichen", 1), item("minecraft:lily_pad", 1)),
            List.of(item("anvilcraft:honey", 100)),
            List.of(), List.of(), List.of()
        ));

        // 茂林星球
        saveSpecialCelestialBody(provider, "forest_planet", new SpecialCelestialBodyRecipe(
            "forest_planet",
            "anvilcraft_dearplus:block/celestial_body/planet_forest",
            true,
            32, 14, 20, 16,
            true, Optional.of(LiquidCoverage.MEDIUM),
            2, 2, 0f,
            List.of(mc("oak_sapling"), mc("birch_sapling"), mc("spruce_sapling"), mc("jungle_sapling"),
                mc("acacia_sapling"), mc("dark_oak_sapling"), mc("mangrove_propagule"), mc("cherry_sapling")),
            List.of(item("minecraft:coal_block", 30), item("minecraft:charcoal", 30), item("minecraft:mud", 15),
                item("anvilcraft:resin", 15), item("anvilcraft:hardend_resin", 8), item("minecraft:diamond", 2)),
            List.of(item("minecraft:water", 100)),
            List.of(item("minecraft:oak_log", 1), item("minecraft:spruce_log", 1), item("minecraft:birch_log", 1),
                item("minecraft:jungle_log", 1), item("minecraft:acacia_log", 1), item("minecraft:dark_oak_log", 1),
                item("minecraft:mangrove_log", 1), item("minecraft:cherry_log", 1), item("minecraft:crimson_stem", 1),
                item("minecraft:warped_stem", 1), item("minecraft:oak_leaves", 1), item("minecraft:spruce_leaves", 1),
                item("minecraft:birch_leaves", 1), item("minecraft:jungle_leaves", 1), item("minecraft:acacia_leaves", 1),
                item("minecraft:dark_oak_leaves", 1), item("minecraft:mangrove_leaves", 1), item("minecraft:cherry_leaves", 1),
                item("minecraft:azalea_leaves", 1), item("minecraft:flowering_azalea_leaves", 1),
                item("minecraft:crimson_wart_block", 1), item("minecraft:warped_wart_block", 1),
                item("minecraft:shroomlight", 1), item("minecraft:apple", 1), item("minecraft:mangrove_roots", 1),
                item("minecraft:hanging_roots", 1), item("minecraft:dead_bush", 1)),
            List.of(),
            List.of(), List.of(), List.of()
        ));

        // 稻果星球
        saveSpecialCelestialBody(provider, "fruit_planet", new SpecialCelestialBodyRecipe(
            "fruit_planet",
            "anvilcraft_dearplus:block/celestial_body/planet_fruit",
            true,
            32, 14, 20, 16,
            true, Optional.of(LiquidCoverage.MEDIUM),
            2, 2, 0f,
            List.of(mc("wheat"), mc("carrot"), mc("potato"), mc("beetroot"), mc("sweet_berries"),
                mc("glow_berries"), mc("cocoa_beans"), mc("melon_slice"), mc("pumpkin")),
            List.of(item("minecraft:coal_block", 30), item("minecraft:stick", 30), item("anvilcraft:flour", 20),
                item("minecraft:rooted_dirt", 5), item("minecraft:bread", 5), item("minecraft:baked_potato", 5),
                item("minecraft:popped_chorus_fruit", 3), item("anvilcraft:exp_gem_block", 2)),
            List.of(item("minecraft:water", 100)),
            List.of(item("minecraft:hay_block", 1), item("minecraft:carrot", 1), item("minecraft:potato", 1),
                item("minecraft:poisonous_potato", 1), item("minecraft:beetroot", 1), item("minecraft:chorus_fruit", 1),
                item("minecraft:cactus", 1), item("minecraft:sugar_cane", 1), item("minecraft:sweet_berries", 1),
                item("minecraft:glow_berries", 1), item("minecraft:apple", 1), item("minecraft:cocoa_beans", 1),
                item("minecraft:melon", 1), item("minecraft:pumpkin", 1), item("minecraft:carved_pumpkin", 1),
                item("minecraft:sea_pickle", 1), item("minecraft:nether_wart", 1), item("minecraft:kelp", 1),
                item("minecraft:brown_mushroom", 1), item("minecraft:red_mushroom", 1), item("minecraft:crimson_fungus", 1),
                item("minecraft:warped_fungus", 1), item("minecraft:bamboo", 1), item("minecraft:moss_block", 1),
                item("minecraft:torchflower_seeds", 1), item("minecraft:pitcher_pod", 1), item("minecraft:weeping_vines", 1),
                item("minecraft:twisting_vines", 1), item("minecraft:vine", 1), item("minecraft:big_dripleaf", 1),
                item("anvilcraft:seeds_pack", 1)),
            List.of(),
            List.of(), List.of(), List.of()
        ));

        // 晶石星球
        saveSpecialCelestialBody(provider, "crystal_planet", new SpecialCelestialBodyRecipe(
            "crystal_planet",
            "anvilcraft_dearplus:block/celestial_body/planet_crystal",
            true,
            40, 14, 20, 15,
            true, Optional.of(LiquidCoverage.MEDIUM),
            2, 1, 23.5f,
            List.of(ResourceLocation.parse("anvilcraft_dearplus:autumnium_alloy_block"), mc("budding_amethyst"),
                ResourceLocation.parse("anvilcraft:ancient_sea_reef"), mc("conduit")),
            List.of(item("minecraft:quartz", 25), item("minecraft:amethyst_shard", 25), item("minecraft:clay_ball", 20),
                item("anvilcraft:lime_powder", 10), item("anvilcraft:ancient_sea_reef", 5), item("anvilcraft:quartz_sand", 5),
                item("anvilcraft:geode", 3), item("minecraft:flint", 2), item("minecraft:pointed_dripstone", 2),
                item("minecraft:echo_shard", 2), item("minecraft:nautilus_shell", 1)),
            List.of(item("minecraft:water", 100), item("minecraft:powder_snow", 100)),
            List.of(),
            List.of(),
            List.of(), List.of(), List.of()
        ));

        // 一摩尔鼹鼠
        saveSpecialCelestialBody(provider, "mole_of_moles", new SpecialCelestialBodyRecipe(
            "mole_of_moles",
            "anvilcraft_dearplus:block/celestial_body/planet_mole",
            true,
            35, 5, 1, 5,
            false, Optional.of(LiquidCoverage.LOW),
            0, 4, 30f,
            List.of(mc("written_book")),
            List.of(item("anvilcraft:rotten_flesh_block", 45), item("minecraft:netherrack", 40),
                item("minecraft:rabbit_hide", 10), item("minecraft:coal", 3), item("anvilcraft:chocolate_black", 2)),
            List.of(item("anvilcraft:oil", 100), item("anvilcraft:primordial_matter", 100)),
            List.of(),
            List.of(),
            List.of(), List.of(), List.of()
        ));
    }

    /// 保存特殊天体配方，配方 id = anvilcraft_dearplus:special_celestial_body/{name}
    private static void saveSpecialCelestialBody(RegistrumRecipeProvider provider, String name, SpecialCelestialBodyRecipe recipe) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("anvilcraft_dearplus", "special_celestial_body/" + name);
        Advancement.Builder advancement = provider.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .requirements(AdvancementRequirements.Strategy.OR);
        provider.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }

    private static ResourceLocation mc(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    private static SpecialCelestialBodyRecipe.WeightedEntry item(String id, int weight) {
        return new SpecialCelestialBodyRecipe.WeightedEntry(id, weight);
    }
}
