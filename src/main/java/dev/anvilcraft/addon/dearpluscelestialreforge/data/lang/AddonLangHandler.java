package dev.anvilcraft.addon.dearpluscelestialreforge.data.lang;

import dev.anvilcraft.addon.dearpluscelestialreforge.AddonConfig;
import dev.anvilcraft.lib.v2.config.ConfigData;
import dev.anvilcraft.lib.v2.registrum.providers.RegistrumLangProvider;

public class AddonLangHandler {

    public static void init(RegistrumLangProvider provider) {
        ConfigData.readConfigClass(provider, AddonConfig.class);

        // GUI title
        provider.add("container.anvilcraft_dearpluscelestialreforge.reforging_panel", "Reforging Panel");

        // Filter names
        provider.add("gui.reforging_panel.filter_magnetic_field", "Mag. Field");
        provider.add("gui.reforging_panel.filter_rotation_speed", "Rot. Speed");
        provider.add("gui.reforging_panel.filter_resource", "Planetary");

        // Magnetic field
        provider.add("gui.reforging_panel.magnetic.none", "None");
        provider.add("gui.reforging_panel.magnetic.barely", "Barely");
        provider.add("gui.reforging_panel.magnetic.weak", "Weak");
        provider.add("gui.reforging_panel.magnetic.medium", "Medium");
        provider.add("gui.reforging_panel.magnetic.strong", "Strong");
        provider.add("gui.reforging_panel.magnetic.very_strong", "Very Strong");

        // Rotation speed
        provider.add("gui.reforging_panel.speed.very_slow", "Very Slow");
        provider.add("gui.reforging_panel.speed.slow", "Slow");
        provider.add("gui.reforging_panel.speed.medium", "Medium");
        provider.add("gui.reforging_panel.speed.fast", "Fast");
        provider.add("gui.reforging_panel.speed.very_fast", "Very Fast");

        // Resource options
        provider.add("gui.reforging_panel.resource.none", "None");
        provider.add("gui.reforging_panel.resource.biological", "Biological");
        provider.add("gui.reforging_panel.resource.civilization", "Civilization");
        provider.add("gui.reforging_panel.resource.any", "Any");

        // Tooltips
        provider.add("item.anvilcraft_dearpluscelestialreforge.autumnium_alloy.desc",
            "A composite material filled with autumn magic");
        provider.add("block.anvilcraft_dearpluscelestialreforge.autumnium_alloy_block.desc",
            "A large block filled with autumn magic");
        provider.add("block.anvilcraft_dearpluscelestialreforge.reforging_panel.desc",
            "Automatically reforges celestial bodies when activated. Configure filters via GUI");
    }
}
