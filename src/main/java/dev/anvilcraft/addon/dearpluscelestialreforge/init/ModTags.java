package dev.anvilcraft.addon.dearpluscelestialreforge.init;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static final TagKey<Item> IS_AUTUMNIUM_COMPONENT = TagKey.create(
        Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(AnvilCraftDearPlusCelestialReforge.MOD_ID, "is_autumnium_component")
    );
}
