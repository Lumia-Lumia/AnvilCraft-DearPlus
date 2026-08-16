package dev.anvilcraft.addon.dearplus.init;

import dev.anvilcraft.addon.dearplus.client.gui.screen.ReforgingPanelScreen;
import dev.anvilcraft.addon.dearplus.inventory.ReforgingPanelMenu;
import dev.anvilcraft.lib.v2.registrum.util.entry.MenuEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import static dev.anvilcraft.addon.dearplus.AnvilCraftDearPlus.REGISTRUM;

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
