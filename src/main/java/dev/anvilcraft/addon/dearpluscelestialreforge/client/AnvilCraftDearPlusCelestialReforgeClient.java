package dev.anvilcraft.addon.dearpluscelestialreforge.client;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.AddonBlocks;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.AddonItems;
import dev.dubhe.anvilcraft.client.renderer.item.ItemSlotClipping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@Mod(value = AnvilCraftDearPlusCelestialReforge.MOD_ID, dist = Dist.CLIENT)
public class AnvilCraftDearPlusCelestialReforgeClient {
    public AnvilCraftDearPlusCelestialReforgeClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(this::registerItemColors);
        // 物品栏渲染裁切：注册需延迟到物品注册完成后（FMLClientSetupEvent），
        // 否则 ItemEntry.get() 在 mod 构造阶段尚未绑定会抛 unbound 异常。
        modBus.addListener(this::registerItemSlotClipping);
    }

    private void registerItemSlotClipping(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemSlotClipping.register(
            AddonItems.AUTUMNIUM_RESONATOR.get(),
            AddonItems.AUTUMNIUM_RING_BLADE_3.get(),
            AddonItems.AUTUMNIUM_RING_BLADE_5.get(),
            AddonItems.AUTUMNIUM_RING_BLADE_7.get(),
            AddonItems.AUTUMNIUM_RING_BLADE_9.get()
        ));
    }

    private void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? 0xB3312C : -1,
            AddonBlocks.REFORGING_PANEL.get());
    }
}
