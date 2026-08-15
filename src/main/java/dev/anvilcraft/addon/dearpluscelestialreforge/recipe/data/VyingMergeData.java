package dev.anvilcraft.addon.dearpluscelestialreforge.recipe.data;

import com.mojang.serialization.MapCodec;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.AddonComponents;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.ModCustomDataComponents;
import dev.dubhe.anvilcraft.api.recipe.data.ICustomDataComponent;
import dev.dubhe.anvilcraft.api.recipe.result.ResultContext;
import dev.dubhe.anvilcraft.api.recipe.slot.RecipeInputSlot;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 两把九环刀合并时的逐鹿等级叠加：
 * 两把原料逐鹿等级相同则新刀等级为原料等级 + 1，不同则取最大值；上限 {@value #MAX_LEVEL} 级。
 */
public class VyingMergeData implements ICustomDataComponent<Integer> {
    public static final int MAX_LEVEL = 10;

    public VyingMergeData() {
    }

    public static VyingMergeData of() {
        return new VyingMergeData();
    }

    @Override
    public DataComponentType<Integer> getDataComponentType() {
        return AddonComponents.VYING;
    }

    @Override
    public Type getType() {
        return ModCustomDataComponents.VYING_MERGE.get();
    }

    @Override
    public Integer make(ResultContext ctx) {
        int a = ctx.getInput(RecipeInputSlot.input(0)).getOrDefault(AddonComponents.VYING, 0);
        int b = ctx.getInput(RecipeInputSlot.input(1)).getOrDefault(AddonComponents.VYING, 0);
        int merged;
        if (a == 0 && b == 0) {
            merged = 0;
        } else if (a == b) {
            merged = Math.min(MAX_LEVEL, a + 1);
        } else {
            merged = Math.max(a, b);
        }
        return merged;
    }

    @Override
    public Integer merge(Integer oldData, Integer newData) {
        return newData;
    }

    public static class Type implements ICustomDataComponent.Type<VyingMergeData> {
        public static final MapCodec<VyingMergeData> CODEC = MapCodec.unit(new VyingMergeData());
        public static final StreamCodec<RegistryFriendlyByteBuf, VyingMergeData> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {}, buf -> new VyingMergeData());

        @Override
        public MapCodec<VyingMergeData> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, VyingMergeData> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
