package dev.anvilcraft.addon.dearpluscelestialreforge.data.tag;

import dev.anvilcraft.addon.dearpluscelestialreforge.init.ModTags;
import dev.anvilcraft.lib.v2.registrum.providers.RegistrumItemTagsProvider;
import net.minecraft.world.item.Items;

public class AddonTagHandler {
    public static void initItemTags(RegistrumItemTagsProvider provider) {
        provider.addTag(ModTags.IS_AUTUMNIUM_COMPONENT)
            .add(Items.BIRCH_LEAVES)
            .add(Items.CHERRY_LEAVES)
            .add(Items.HORN_CORAL)
            .add(Items.HORN_CORAL_FAN);
    }
}
