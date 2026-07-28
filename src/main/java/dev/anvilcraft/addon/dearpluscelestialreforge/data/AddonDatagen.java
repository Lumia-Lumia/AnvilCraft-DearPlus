package dev.anvilcraft.addon.dearpluscelestialreforge.data;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.data.lang.AddonLangHandler;
import dev.anvilcraft.addon.dearpluscelestialreforge.data.tag.AddonTagHandler;
import dev.anvilcraft.lib.v2.registrum.providers.ProviderType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge.REGISTRUM;

@EventBusSubscriber(modid = AnvilCraftDearPlusCelestialReforge.MOD_ID)
public class AddonDatagen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {}

    /**
     * 初始化生成器
     */
    public static void init() {
        REGISTRUM.addDataGenerator(ProviderType.LANG, AddonLangHandler::init);
        REGISTRUM.addDataGenerator(ProviderType.ITEM_TAGS, AddonTagHandler::initItemTags);
    }
}
