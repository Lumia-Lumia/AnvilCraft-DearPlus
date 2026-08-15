package dev.anvilcraft.addon.dearpluscelestialreforge.data;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.data.lang.AddonLangHandler;
import dev.anvilcraft.addon.dearpluscelestialreforge.data.recipe.AddonRecipeHandler;
import dev.anvilcraft.addon.dearpluscelestialreforge.data.tag.AddonTagHandler;
import dev.anvilcraft.lib.v2.registrum.providers.ProviderType;

import static dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge.REGISTRUM;

public class AddonDatagen {
    /**
     * 初始化生成器
     */
    public static void init() {
        REGISTRUM.addDataGenerator(ProviderType.LANG, AddonLangHandler::init);
        REGISTRUM.addDataGenerator(ProviderType.ITEM_TAGS, AddonTagHandler::initItemTags);
        REGISTRUM.addDataGenerator(ProviderType.RECIPE, AddonRecipeHandler::init);
    }
}
