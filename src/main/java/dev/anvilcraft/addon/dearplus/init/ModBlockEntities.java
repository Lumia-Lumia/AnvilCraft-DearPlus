package dev.anvilcraft.addon.dearplus.init;

import dev.anvilcraft.addon.dearplus.block.entity.ReforgingPanelBlockEntity;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;

import static dev.anvilcraft.addon.dearplus.AnvilCraftDearPlus.REGISTRUM;

public class ModBlockEntities {
    public static final BlockEntityEntry<ReforgingPanelBlockEntity> REFORGING_PANEL = REGISTRUM
        .blockEntity("reforging_panel", ReforgingPanelBlockEntity::new)
        .validBlock(AddonBlocks.REFORGING_PANEL)
        .register();

    public static void register() {
    }
}
