package dev.anvilcraft.addon.dearpluscelestialreforge.recipe.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.AddonComponents;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.ModCustomDataComponents;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.property.component.BladeAffixes;
import dev.dubhe.anvilcraft.api.recipe.data.ICustomDataComponent;
import dev.dubhe.anvilcraft.api.recipe.result.ResultContext;
import dev.dubhe.anvilcraft.api.recipe.slot.RecipeInputSlot;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 从指定输入槽读取金属粒并生成破竹/枭首/豪夺词条等级的自定义数据组件。
 *
 * <p>用于二合一锻造：铁粒→破竹、铜粒→枭首、金粒→豪夺，只按金属粒**种类** +1 级
 * （不随堆叠数量累加），并与结果物品上已有的词条逐项累加。</p>
 */
public class BladeAffixesData implements ICustomDataComponent<BladeAffixes> {
    private final RecipeInputSlot input;

    private BladeAffixesData(RecipeInputSlot input) {
        this.input = input;
    }

    public static BladeAffixesData of(int index) {
        return new BladeAffixesData(RecipeInputSlot.input(index));
    }

    public static BladeAffixesData of(RecipeInputSlot slot) {
        return new BladeAffixesData(slot);
    }

    public RecipeInputSlot getInput() {
        return this.input;
    }

    @Override
    public DataComponentType<BladeAffixes> getDataComponentType() {
        return AddonComponents.BLADE_AFFIXES;
    }

    @Override
    public Type getType() {
        return ModCustomDataComponents.BLADE_AFFIXES.get();
    }

    @Override
    public BladeAffixes make(ResultContext ctx) {
        ItemStack stack = ctx.getInput(this.input);
        if (stack.is(Items.IRON_NUGGET)) {
            return new BladeAffixes(1, 0, 0);
        } else if (stack.is(ModItems.COPPER_NUGGET.get())) {
            return new BladeAffixes(0, 1, 0);
        } else if (stack.is(Items.GOLD_NUGGET)) {
            return new BladeAffixes(0, 0, 1);
        }
        return BladeAffixes.EMPTY;
    }

    @Override
    public BladeAffixes merge(BladeAffixes oldData, BladeAffixes newData) {
        BladeAffixes oldValue = oldData == null ? BladeAffixes.EMPTY : oldData;
        BladeAffixes newValue = newData == null ? BladeAffixes.EMPTY : newData;
        return oldValue.add(newValue);
    }

    public static class Type implements ICustomDataComponent.Type<BladeAffixesData> {
        public static final MapCodec<BladeAffixesData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RecipeInputSlot.CODEC.forGetter(BladeAffixesData::getInput)
        ).apply(instance, BladeAffixesData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, BladeAffixesData> STREAM_CODEC =
            StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, data -> data.getInput().getSerializedName(),
                name -> new BladeAffixesData(new RecipeInputSlot(name))
            );

        @Override
        public MapCodec<BladeAffixesData> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BladeAffixesData> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
