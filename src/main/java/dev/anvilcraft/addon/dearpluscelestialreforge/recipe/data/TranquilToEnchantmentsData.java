package dev.anvilcraft.addon.dearpluscelestialreforge.recipe.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.AddonComponents;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.ModCustomDataComponents;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.property.component.AffixHelper;
import dev.dubhe.anvilcraft.api.recipe.data.ICustomDataComponent;
import dev.dubhe.anvilcraft.api.recipe.result.ResultContext;
import dev.dubhe.anvilcraft.api.recipe.slot.RecipeInputSlot;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * 把偏安物品被吸收的附魔恢复到结果物品的 {@link DataComponents#ENCHANTMENTS}。
 *
 * <p>用于七环刀 → 九环刀的二合一锻造：九环刀不再持有「偏安」，因此把七环刀被吸收的
 * 附魔移回 {@code ENCHANTMENTS}，使其重新生效。</p>
 */
public class TranquilToEnchantmentsData implements ICustomDataComponent<ItemEnchantments> {
    private final RecipeInputSlot input;

    private TranquilToEnchantmentsData(RecipeInputSlot input) {
        this.input = input;
    }

    public static TranquilToEnchantmentsData of(RecipeInputSlot slot) {
        return new TranquilToEnchantmentsData(slot);
    }

    public RecipeInputSlot getInput() {
        return this.input;
    }

    @Override
    public DataComponentType<ItemEnchantments> getDataComponentType() {
        return DataComponents.ENCHANTMENTS;
    }

    @Override
    public Type getType() {
        return ModCustomDataComponents.TRANQUIL_TO_ENCHANTMENTS.get();
    }

    @Override
    public ItemEnchantments make(ResultContext ctx) {
        return AffixHelper.getTranquilEnchantments(ctx.getInput(this.input));
    }

    @Override
    public ItemEnchantments merge(ItemEnchantments oldData, ItemEnchantments newData) {
        return newData == null ? ItemEnchantments.EMPTY : newData;
    }

    public static class Type implements ICustomDataComponent.Type<TranquilToEnchantmentsData> {
        public static final MapCodec<TranquilToEnchantmentsData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RecipeInputSlot.CODEC.forGetter(TranquilToEnchantmentsData::getInput)
        ).apply(instance, TranquilToEnchantmentsData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, TranquilToEnchantmentsData> STREAM_CODEC =
            StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, data -> data.getInput().getSerializedName(),
                name -> new TranquilToEnchantmentsData(new RecipeInputSlot(name))
            );

        @Override
        public MapCodec<TranquilToEnchantmentsData> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TranquilToEnchantmentsData> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
