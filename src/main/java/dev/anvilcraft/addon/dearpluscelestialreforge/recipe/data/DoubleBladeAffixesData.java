package dev.anvilcraft.addon.dearpluscelestialreforge.recipe.data;

import com.mojang.serialization.MapCodec;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.AddonComponents;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.ModCustomDataComponents;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.RingedAutumniumBroadswordItem;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.property.component.AffixHelper;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.property.component.BladeAffixes;
import dev.dubhe.anvilcraft.api.recipe.data.ICustomDataComponent;
import dev.dubhe.anvilcraft.api.recipe.result.ResultContext;
import dev.dubhe.anvilcraft.api.recipe.slot.RecipeInputSlot;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * 两把九环刀合并时的词条叠加：破竹/枭首/豪夺等级取两把之和，上限 {@value #MAX_LEVEL} 级。
 *
 * <p>用于二合一锻造：材料槽放入超限多相合金，输入槽放入两把九环刀。</p>
 */
public class DoubleBladeAffixesData implements ICustomDataComponent<BladeAffixes> {
    public static final int MAX_LEVEL = 255;

    public DoubleBladeAffixesData() {
    }

    public static DoubleBladeAffixesData of() {
        return new DoubleBladeAffixesData();
    }

    @Override
    public DataComponentType<BladeAffixes> getDataComponentType() {
        return AddonComponents.BLADE_AFFIXES;
    }

    @Override
    public Type getType() {
        return ModCustomDataComponents.DOUBLE_BLADE_AFFIXES.get();
    }

    @Override
    public BladeAffixes make(ResultContext ctx) {
        BladeAffixes a = affixesOf(ctx.getInput(RecipeInputSlot.input(0)));
        BladeAffixes b = affixesOf(ctx.getInput(RecipeInputSlot.input(1)));
        return new BladeAffixes(
            Math.min(MAX_LEVEL, a.chopper() + b.chopper()),
            Math.min(MAX_LEVEL, a.decapitator() + b.decapitator()),
            Math.min(MAX_LEVEL, a.dispossessor() + b.dispossessor())
        );
    }

    private static BladeAffixes affixesOf(ItemStack stack) {
        if (stack.getItem() instanceof RingedAutumniumBroadswordItem) {
            return AffixHelper.getBladeAffixes(stack);
        }
        return BladeAffixes.EMPTY;
    }

    @Override
    public BladeAffixes merge(BladeAffixes oldData, BladeAffixes newData) {
        // 结果刀初始无词条，直接用两把之和
        return newData;
    }

    public static class Type implements ICustomDataComponent.Type<DoubleBladeAffixesData> {
        public static final MapCodec<DoubleBladeAffixesData> CODEC = MapCodec.unit(new DoubleBladeAffixesData());
        public static final StreamCodec<RegistryFriendlyByteBuf, DoubleBladeAffixesData> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {}, buf -> new DoubleBladeAffixesData());

        @Override
        public MapCodec<DoubleBladeAffixesData> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DoubleBladeAffixesData> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
