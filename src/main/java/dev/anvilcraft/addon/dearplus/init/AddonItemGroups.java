package dev.anvilcraft.addon.dearplus.init;

import dev.anvilcraft.addon.dearplus.AnvilCraftDearPlus;
import dev.dubhe.anvilcraft.init.item.ModItemGroups;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static dev.anvilcraft.addon.dearplus.AnvilCraftDearPlus.REGISTRUM;


public class AddonItemGroups {
    private static final DeferredRegister<CreativeModeTab> DEFERRED_REGISTER = DeferredRegister.create(
        Registries.CREATIVE_MODE_TAB,
        AnvilCraftDearPlus.MOD_ID
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ADDON_ITEMS = DEFERRED_REGISTER.register(
        "addon_items",
        () -> CreativeModeTab.builder()
            .icon(AddonItems.AUTUMNIUM_ALLOY::asStack)
            // 物品由 REGISTRUM.defaultCreativeTab 自动填充到该标签页
            .displayItems((ctx, entries) -> {
            })
            .title(
                REGISTRUM.addLang(
                    "itemGroup",
                    AnvilCraftDearPlus.of("addon_items"),
                    "AnvilCraft: DearPlus"
                )
            )
            .withTabsBefore(ModItemGroups.ANVILCRAFT_BUILD_BLOCK.getId())
            .build()
    );

    public static void register(IEventBus modEventBus) {
        DEFERRED_REGISTER.register(modEventBus);
    }
}
