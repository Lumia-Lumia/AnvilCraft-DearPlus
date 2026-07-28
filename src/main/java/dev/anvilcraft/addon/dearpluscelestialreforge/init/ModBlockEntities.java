package dev.anvilcraft.addon.dearpluscelestialreforge.init;

import dev.anvilcraft.addon.dearpluscelestialreforge.block.entity.ReforgingPanelBlockEntity;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;

import static dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge.REGISTRUM;

public class ModBlockEntities {
    public static final BlockEntityEntry<ReforgingPanelBlockEntity> REFORGING_PANEL = REGISTRUM
        .blockEntity("reforging_panel", ReforgingPanelBlockEntity::new)
        .validBlock(AddonBlocks.REFORGING_PANEL)
        .register();

    public static void register() {
    }
}
