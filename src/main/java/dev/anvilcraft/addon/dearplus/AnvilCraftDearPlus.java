package dev.anvilcraft.addon.dearplus;

import com.mojang.logging.LogUtils;
import dev.anvilcraft.addon.dearplus.data.AddonDatagen;
import dev.anvilcraft.addon.dearplus.init.AddonBlocks;
import dev.anvilcraft.addon.dearplus.init.AddonComponents;
import dev.anvilcraft.addon.dearplus.init.AddonItemGroups;
import dev.anvilcraft.addon.dearplus.init.AddonItems;
import dev.anvilcraft.addon.dearplus.init.AddonOutcomeTypes;
import dev.anvilcraft.addon.dearplus.init.ModBlockEntities;
import dev.anvilcraft.addon.dearplus.init.ModCustomDataComponents;
import dev.anvilcraft.addon.dearplus.init.ModMenuTypes;
import dev.anvilcraft.addon.dearplus.init.ModRecipeSerializers;
import dev.anvilcraft.lib.v2.config.ConfigManager;
import dev.anvilcraft.lib.v2.network.register.NetworkRegistrar;
import dev.anvilcraft.lib.v2.registrum.Registrum;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

@Mod(AnvilCraftDearPlus.MOD_ID)
public class AnvilCraftDearPlus {
    public static final String MOD_ID = "anvilcraft_dearplus";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final AddonConfig CONFIG = ConfigManager.register(AnvilCraftDearPlus.MOD_ID, AddonConfig::new);
    public static final Registrum REGISTRUM = Registrum.create(MOD_ID);

    public AnvilCraftDearPlus(IEventBus modEventBus, ModContainer modContainer) {
        AddonItemGroups.register(modEventBus);
        AddonComponents.register(modEventBus);
        ModCustomDataComponents.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        AddonOutcomeTypes.register(modEventBus);
        modEventBus.addListener(this::registerPayloadHandlers);
        AddonBlocks.register();
        AddonItems.register();
        ModBlockEntities.register();
        ModMenuTypes.register();
        AddonDatagen.init();
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        NetworkRegistrar.register(event.registrar(MOD_ID), MOD_ID);
    }

    public static ResourceLocation of(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
