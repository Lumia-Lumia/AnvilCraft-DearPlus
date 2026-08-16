package dev.anvilcraft.addon.dearplus.recipe.data;

import com.mojang.serialization.MapCodec;
import dev.anvilcraft.addon.dearplus.init.ModCustomDataComponents;
import dev.dubhe.anvilcraft.api.recipe.data.ICustomDataComponent;
import dev.dubhe.anvilcraft.api.recipe.result.ResultContext;
import dev.dubhe.anvilcraft.init.item.ModComponents;
import dev.dubhe.anvilcraft.item.property.component.Eternal;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 二合一锻造（两把九环刀合并）时给结果加入「永恒」词条（铁砧工艺词条，效果是无法被破坏）。
 *
 * <p>若原料已持有永恒则保留，否则结果必带永恒，因此恒返回 {@link Eternal#INSTANCE}。</p>
 */
public class EternalMergeData implements ICustomDataComponent<Eternal> {
    public EternalMergeData() {
    }

    public static EternalMergeData of() {
        return new EternalMergeData();
    }

    @Override
    public DataComponentType<Eternal> getDataComponentType() {
        return ModComponents.ETERNAL;
    }

    @Override
    public Type getType() {
        return ModCustomDataComponents.ETERNAL_MERGE.get();
    }

    @Override
    public Eternal make(ResultContext ctx) {
        return Eternal.INSTANCE;
    }

    @Override
    public Eternal merge(Eternal oldData, Eternal newData) {
        return newData;
    }

    public static class Type implements ICustomDataComponent.Type<EternalMergeData> {
        public static final MapCodec<EternalMergeData> CODEC = MapCodec.unit(new EternalMergeData());
        public static final StreamCodec<RegistryFriendlyByteBuf, EternalMergeData> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {}, buf -> new EternalMergeData());

        @Override
        public MapCodec<EternalMergeData> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EternalMergeData> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
