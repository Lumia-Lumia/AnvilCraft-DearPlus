package dev.anvilcraft.addon.dearpluscelestialreforge.init;

import dev.anvilcraft.lib.v2.registrum.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

import static dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge.REGISTRUM;

public class AddonItems {
    static {
        REGISTRUM.defaultCreativeTab(AddonItemGroups.ADDON_ITEMS.getKey());
    }

    public static final ItemEntry<Item> AUTUMNIUM_ALLOY = REGISTRUM
        .item("autumnium_alloy", Item::new)
        .lang("Autumnium Alloy")
        .register();

    public static void register() {
    }
}
