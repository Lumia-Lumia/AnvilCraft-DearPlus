package dev.anvilcraft.addon.dearplus.data;

import dev.anvilcraft.addon.dearplus.AnvilCraftDearPlus;
import dev.anvilcraft.addon.dearplus.data.lang.AddonLangHandler;
import dev.anvilcraft.addon.dearplus.data.recipe.AddonRecipeHandler;
import dev.anvilcraft.addon.dearplus.data.tag.AddonTagHandler;
import dev.anvilcraft.lib.v2.registrum.providers.ProviderType;

import static dev.anvilcraft.addon.dearplus.AnvilCraftDearPlus.REGISTRUM;

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
