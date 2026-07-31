package dev.anvilcraft.addon.dearpluscelestialreforge.event;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = AnvilCraftDearPlusCelestialReforge.MOD_ID)
public class AddonTooltipEventListener {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        String key = stack.getItem().builtInRegistryHolder().key().location().toString();
        if (!key.startsWith(AnvilCraftDearPlusCelestialReforge.MOD_ID)) return;
        String descKey = stack.getDescriptionId() + ".desc";
        event.getToolTip().add(Component.translatable(descKey).withStyle(ChatFormatting.GRAY));
    }
}
