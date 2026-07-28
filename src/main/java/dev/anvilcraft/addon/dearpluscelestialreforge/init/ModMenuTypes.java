package dev.anvilcraft.addon.dearpluscelestialreforge.init;

import dev.anvilcraft.addon.dearpluscelestialreforge.client.gui.screen.ReforgingPanelScreen;
import dev.anvilcraft.addon.dearpluscelestialreforge.inventory.ReforgingPanelMenu;
import dev.anvilcraft.lib.v2.registrum.util.entry.MenuEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import static dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge.REGISTRUM;

public class ModMenuTypes {
    public static final MenuEntry<ReforgingPanelMenu> REFORGING_PANEL = REGISTRUM
        .menu("reforging_panel",
            (MenuType<ReforgingPanelMenu> type, int windowId, Inventory inv, RegistryFriendlyByteBuf buf) ->
                new ReforgingPanelMenu(type, windowId, inv, buf),
            () -> ReforgingPanelScreen::new)
        .register();

    public static void register() {
    }
}
