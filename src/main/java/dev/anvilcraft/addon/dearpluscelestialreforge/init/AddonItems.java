package dev.anvilcraft.addon.dearpluscelestialreforge.init;

import com.mojang.datafixers.util.Unit;
import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.AutumniumIonocraftItem;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.AutumniumResonatorItem;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.RingedAutumniumBroadswordItem;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.property.component.BladeAffixes;
import dev.anvilcraft.lib.v2.registrum.providers.RegistrumRecipeProvider;
import dev.anvilcraft.lib.v2.registrum.util.entry.ItemEntry;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import static dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge.REGISTRUM;

public class AddonItems {
    static {
        REGISTRUM.defaultCreativeTab(AddonItemGroups.ADDON_ITEMS.getKey());
    }

    public static final ItemEntry<Item> AUTUMNIUM_ALLOY = REGISTRUM
        .item("autumnium_alloy", Item::new)
        .lang("Autumnium Alloy")
        .register();

    public static final ItemEntry<AutumniumResonatorItem> AUTUMNIUM_RESONATOR = REGISTRUM
        .item("autumnium_resonator", AutumniumResonatorItem::new)
        .lang("Autumnium Resonator")
        // 临时外观：复用铁砧工艺余烬共振器，禁用自动生成的模型
        .model((ctx, prov) -> {})
        .recipe((ctx, prov) -> {
            // 秋枫合金，秋枫合金块，空 / 空，避雷针，空 / 空，秋枫合金，空
            ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ctx.getEntry())
                .pattern("AB ")
                .pattern(" C ")
                .pattern(" A ")
                .define('A', AddonItems.AUTUMNIUM_ALLOY.get())
                .define('B', AddonBlocks.AUTUMNIUM_ALLOY_BLOCK.get())
                .define('C', Items.LIGHTNING_ROD)
                .unlockedBy("has_autumnium_alloy",
                    RegistrumRecipeProvider.has(AddonItems.AUTUMNIUM_ALLOY.get()))
                .save(prov);

            // 工作台 + 1 个秋枫合金块 → 满耐久（无序配方）
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ctx.getEntry())
                .requires(ctx.getEntry())
                .requires(AddonBlocks.AUTUMNIUM_ALLOY_BLOCK.get())
                .unlockedBy("has_autumnium_resonator",
                    RegistrumRecipeProvider.has(ctx.getEntry()))
                .save(prov, AnvilCraftDearPlusCelestialReforge.of("autumnium_resonator_repair"));
        })
        .register();

    public static final ItemEntry<AutumniumIonocraftItem> AUTUMNIUM_GLIDER = REGISTRUM
        .item("autumnium_ionocraft", AutumniumIonocraftItem::new)
        .lang("Autumnium Ionocraft")
        // 临时外观：复用铁砧工艺飘升机背包，禁用自动生成的模型
        .model((ctx, prov) -> {})
        .recipe((ctx, prov) -> {
            // 羽毛，秋枫合金块，羽毛 / 羽毛，秋枫合金块，羽毛 / 铜压力板，皮革外套，铜压力板
            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ctx.getEntry())
                .pattern("ABA")
                .pattern("ABA")
                .pattern("CDC")
                .define('A', Items.FEATHER)
                .define('B', AddonBlocks.AUTUMNIUM_ALLOY_BLOCK.get())
                .define('C', ModBlocks.COPPER_PRESSURE_PLATE.get())
                .define('D', Items.LEATHER_CHESTPLATE)
                .unlockedBy("has_autumnium_alloy_block",
                    RegistrumRecipeProvider.has(AddonBlocks.AUTUMNIUM_ALLOY_BLOCK.get()))
                .save(prov);

            // 工作台 + 1 个秋枫合金块 → 满耐久（无序配方）
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ctx.getEntry())
                .requires(ctx.getEntry())
                .requires(AddonBlocks.AUTUMNIUM_ALLOY_BLOCK.get())
                .unlockedBy("has_autumnium_ionocraft",
                    RegistrumRecipeProvider.has(ctx.getEntry()))
                .save(prov, AnvilCraftDearPlusCelestialReforge.of("autumnium_ionocraft_repair"));
        })
        .register();

    /** 三环刀：耐久 480，攻击 6，攻速 1.6，默认词条 破竹1/枭首1/豪夺1 */
    public static final ItemEntry<RingedAutumniumBroadswordItem> AUTUMNIUM_RING_BLADE_3 =
        ringBlade("ringed_autumnium_broadsword_3", "3-Ringed Autumnium Broadsword", 480, 6, 1.6f, false, new BladeAffixes(1, 1, 1));
    /** 五环刀：耐久 540，攻击 7，攻速 1.8，默认词条 破竹2/枭首2/豪夺1 */
    public static final ItemEntry<RingedAutumniumBroadswordItem> AUTUMNIUM_RING_BLADE_5 =
        ringBlade("ringed_autumnium_broadsword_5", "5-Ringed Autumnium Broadsword", 540, 7, 1.8f, false, new BladeAffixes(2, 2, 1));
    /** 七环刀：耐久 800，攻击 8，攻速 1.8，默认词条 破竹3/枭首2/豪夺2 */
    public static final ItemEntry<RingedAutumniumBroadswordItem> AUTUMNIUM_RING_BLADE_7 =
        ringBlade("ringed_autumnium_broadsword_7", "7-Ringed Autumnium Broadsword", 800, 8, 1.8f, false, new BladeAffixes(3, 2, 2));
    /** 九环刀：耐久 1999，攻击 12，攻速 2.0，逐鹿（可附魔），默认词条 破竹3/枭首3/豪夺3 */
    public static final ItemEntry<RingedAutumniumBroadswordItem> AUTUMNIUM_RING_BLADE_9 =
        ringBlade("ringed_autumnium_broadsword_9", "9-Ringed Autumnium Broadsword", 1999, 12, 2.0f, true, new BladeAffixes(3, 3, 3));

    /**
     * 注册一把秋枫大环刀。
     *
     * @param vying   是否为九环刀（持「逐鹿」而非「偏安」）
     * @param affixes 默认词条（破竹/枭首/豪夺等级）
     */
    private static ItemEntry<RingedAutumniumBroadswordItem> ringBlade(
        String name, String lang, int durability, int attackDamage, float attackSpeed, boolean vying, BladeAffixes affixes
    ) {
        return REGISTRUM
            .item(name, p -> {
                Item.Properties props = p.durability(durability);
                props.component(AddonComponents.BLADE_AFFIXES, affixes);
                if (vying) {
                    // 逐鹿：初始等级 1，免除经验消耗，随等级提升稀有掉落概率；可在二合一锻造中升级
                    props.component(AddonComponents.VYING, 1);
                } else {
                    props.component(AddonComponents.TRANQUIL, Unit.INSTANCE);
                }
                return new RingedAutumniumBroadswordItem(
                    AddonTiers.AUTUMNIUM,
                    // SwordItem 实际攻击 = 1 + modifier + tierBonus，故 modifier = attackDamage - 1 - tierBonus，
                    // 使实际攻击恰好等于传入的 attackDamage 参数（6/7/8/12）
                    attackDamage - 1 - (int) AddonTiers.AUTUMNIUM.getAttackDamageBonus(),
                    attackSpeed - 4.0f,
                    props
                );
            })
            .lang(lang)
            // 临时外观：复用铁砧工艺余烬金属剑，禁用自动生成的模型
            .model((ctx, prov) -> {})
            .register();
    }

    public static void register() {
    }
}
