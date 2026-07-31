package dev.anvilcraft.addon.dearpluscelestialreforge.client;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.AddonBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@Mod(value = AnvilCraftDearPlusCelestialReforge.MOD_ID, dist = Dist.CLIENT)
public class AnvilCraftDearPlusCelestialReforgeClient {
    public AnvilCraftDearPlusCelestialReforgeClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(this::registerItemColors);
    }

    private void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? 0xB3312C : -1,
            AddonBlocks.REFORGING_PANEL.get());
    }
}
