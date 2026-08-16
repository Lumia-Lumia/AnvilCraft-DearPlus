package dev.anvilcraft.addon.dearplus.data.lang;

import dev.anvilcraft.addon.dearplus.AddonConfig;
import dev.anvilcraft.lib.v2.config.ConfigData;
import dev.anvilcraft.lib.v2.registrum.providers.RegistrumLangProvider;

public class AddonLangHandler {

    public static void init(RegistrumLangProvider provider) {
        ConfigData.readConfigClass(provider, AddonConfig.class);

        // Affixes（格式参考铁砧工艺：词条名 + 冒号 + 描述）
        provider.add("affix.anvilcraft_dearplus.tranquil",
            "Tranquil: Cannot be enchanted or disenchanted; all enchantments are disabled");
        provider.add("affix.anvilcraft_dearplus.vying",
            "Vying %s: Enchanting costs no experience; boosts rare drops with level");
        provider.add("affix.anvilcraft_dearplus.chopper",
            "Chopper %s: Makes the sword deal more damage");
        provider.add("affix.anvilcraft_dearplus.decapitator",
            "Decapitator %s: Beheading effect independent of the Beheading enchantment");
        provider.add("affix.anvilcraft_dearplus.dispossessor",
            "Dispossessor %s: Increases the amount of drops");

        // GUI title
        provider.add("container.anvilcraft_dearplus.reforging_panel", "Reforging Panel");

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
        provider.add("item.anvilcraft_dearplus.autumnium_alloy.desc",
            "A composite material filled with autumn magic");
        provider.add("block.anvilcraft_dearplus.autumnium_alloy_block.desc",
            "A large block filled with autumn magic");
        provider.add("block.anvilcraft_dearplus.reforging_panel.desc",
            "Automatically reforges celestial bodies when activated. Configure filters via GUI");

        provider.add("item.anvilcraft_dearplus.autumnium_resonator.desc",
            "Compatible with all tool types. Hold right-click to resonate-mine most blocks. Cannot switch tool types");
        provider.add("item.anvilcraft_dearplus.autumnium_ionocraft.desc",
            "Grants creative flight when worn\nConsumes durability over time, like an elytra");
        provider.add("item.anvilcraft_dearplus.ringed_autumnium_broadsword_3.desc",
            "A Chinese greatsword with the True Sweep property");
        provider.add("item.anvilcraft_dearplus.ringed_autumnium_broadsword_5.desc",
            "A Chinese greatsword with the True Sweep property");
        provider.add("item.anvilcraft_dearplus.ringed_autumnium_broadsword_7.desc",
            "A Chinese greatsword with the True Sweep property");
        provider.add("item.anvilcraft_dearplus.ringed_autumnium_broadsword_9.desc",
            "A Chinese greatsword with the True Sweep property");
    }
}
