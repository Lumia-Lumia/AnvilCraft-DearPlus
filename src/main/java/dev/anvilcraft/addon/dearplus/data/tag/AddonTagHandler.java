package dev.anvilcraft.addon.dearplus.data.tag;

import dev.anvilcraft.addon.dearplus.init.AddonItems;
import dev.anvilcraft.addon.dearplus.init.ModTags;
import dev.anvilcraft.lib.v2.registrum.providers.RegistrumItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

public class AddonTagHandler {
    public static void initItemTags(RegistrumItemTagsProvider provider) {
        provider.addTag(ModTags.IS_AUTUMNIUM_COMPONENT)
            .add(Items.BIRCH_LEAVES)
            .add(Items.CHERRY_LEAVES)
            .add(Items.HORN_CORAL)
            .add(Items.HORN_CORAL_FAN);

        provider.addTag(ModTags.AUTUMNIUM_RING_BLADES)
            .add(AddonItems.AUTUMNIUM_RING_BLADE_3.get())
            .add(AddonItems.AUTUMNIUM_RING_BLADE_5.get())
            .add(AddonItems.AUTUMNIUM_RING_BLADE_7.get())
            .add(AddonItems.AUTUMNIUM_RING_BLADE_9.get());

        // 大环刀加入原版「剑」标签：使附魔台/铁砧能识别其为可附魔武器
        // （附魔台 isPrimaryItem / 铁砧 canEnchant 均依赖 #minecraft:enchantable/* 标签，
        //  而它们都通过 #minecraft:swords 传递包含；偏安物品的附魔限制由 mixin 另行拦截）
        provider.addTag(ItemTags.SWORDS)
            .add(AddonItems.AUTUMNIUM_RING_BLADE_3.get())
            .add(AddonItems.AUTUMNIUM_RING_BLADE_5.get())
            .add(AddonItems.AUTUMNIUM_RING_BLADE_7.get())
            .add(AddonItems.AUTUMNIUM_RING_BLADE_9.get());

        // 大环刀合成金属粒：铁粒 / 金粒 / 铜粒
        provider.addTag(ModTags.IS_BROADSWORD_RING_COMPONENT)
            .add(Items.IRON_NUGGET)
            .add(Items.GOLD_NUGGET)
            .add(dev.dubhe.anvilcraft.init.item.ModItems.COPPER_NUGGET.get());
    }
}
