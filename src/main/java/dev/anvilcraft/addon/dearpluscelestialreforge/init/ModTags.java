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

    /** 全部秋枫大环刀（三环/五环/七环/九环） */
    public static final TagKey<Item> AUTUMNIUM_RING_BLADES = TagKey.create(
        Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(AnvilCraftDearPlusCelestialReforge.MOD_ID, "ringed_autumnium_broadswords")
    );

    /** 合成大环刀可用的金属粒（铁粒/金粒/铜粒） */
    public static final TagKey<Item> IS_BROADSWORD_RING_COMPONENT = TagKey.create(
        Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(AnvilCraftDearPlusCelestialReforge.MOD_ID, "is_broadsword_ring_component")
    );
}
