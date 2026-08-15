package dev.anvilcraft.addon.dearpluscelestialreforge.recipe.data;

import com.mojang.serialization.MapCodec;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.ModCustomDataComponents;
import dev.dubhe.anvilcraft.api.recipe.data.ICustomDataComponent;
import dev.dubhe.anvilcraft.api.recipe.result.ResultContext;
import dev.dubhe.anvilcraft.api.recipe.slot.RecipeInputSlot;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * 两把九环刀合并时的附魔继承：每个魔咒等级取两把原料的最大值，不考虑魔咒是否互斥。
 */
public class EnchantmentMaxMergeData implements ICustomDataComponent<ItemEnchantments> {
    public EnchantmentMaxMergeData() {
    }

    public static EnchantmentMaxMergeData of() {
        return new EnchantmentMaxMergeData();
    }

    @Override
    public DataComponentType<ItemEnchantments> getDataComponentType() {
        return DataComponents.ENCHANTMENTS;
    }

    @Override
    public Type getType() {
        return ModCustomDataComponents.ENCHANTMENT_MAX_MERGE.get();
    }

    @Override
    public ItemEnchantments make(ResultContext ctx) {
        ItemEnchantments a = ctx.getInput(RecipeInputSlot.input(0)).getEnchantments();
        ItemEnchantments b = ctx.getInput(RecipeInputSlot.input(1)).getEnchantments();
        return maxEnchantments(a, b);
    }

    private static ItemEnchantments maxEnchantments(ItemEnchantments a, ItemEnchantments b) {
        ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(a);
        for (Holder<Enchantment> holder : b.keySet()) {
            int level = b.getLevel(holder);
            if (level > merged.getLevel(holder)) {
                merged.set(holder, level);
            }
        }
        return merged.toImmutable();
    }

    @Override
    public ItemEnchantments merge(ItemEnchantments oldData, ItemEnchantments newData) {
        return newData;
    }

    public static class Type implements ICustomDataComponent.Type<EnchantmentMaxMergeData> {
        public static final MapCodec<EnchantmentMaxMergeData> CODEC = MapCodec.unit(new EnchantmentMaxMergeData());
        public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentMaxMergeData> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {}, buf -> new EnchantmentMaxMergeData());

        @Override
        public MapCodec<EnchantmentMaxMergeData> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EnchantmentMaxMergeData> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
