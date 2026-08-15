package dev.anvilcraft.addon.dearpluscelestialreforge.item.property.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 刀类武器的三种词条等级。
 *
 * @param chopper 破竹等级（效果同锋利）
 * @param decapitator   枭首等级（效果同斩首）
 * @param dispossessor     豪夺等级（效果同抢夺）
 */
public record BladeAffixes(int chopper, int decapitator, int dispossessor) {
    public static final BladeAffixes EMPTY = new BladeAffixes(0, 0, 0);

    /** 词条等级上限（与二合一合并 {@link dev.anvilcraft.addon.dearpluscelestialreforge.recipe.data.DoubleBladeAffixesData} 的 MAX_LEVEL 一致） */
    public static final int MAX_LEVEL = 255;

    public static final MapCodec<BladeAffixes> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.INT.fieldOf("chopper").forGetter(BladeAffixes::chopper),
        Codec.INT.fieldOf("decapitator").forGetter(BladeAffixes::decapitator),
        Codec.INT.fieldOf("dispossessor").forGetter(BladeAffixes::dispossessor)
    ).apply(instance, BladeAffixes::new));

    public static final StreamCodec<ByteBuf, BladeAffixes> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, BladeAffixes::chopper,
        ByteBufCodecs.VAR_INT, BladeAffixes::decapitator,
        ByteBufCodecs.VAR_INT, BladeAffixes::dispossessor,
        BladeAffixes::new
    );

    /** 三种词条等级之和 */
    public int total() {
        return this.chopper + this.decapitator + this.dispossessor;
    }

    public boolean isEmpty() {
        return this.chopper == 0 && this.decapitator == 0 && this.dispossessor == 0;
    }

    /** 逐项累加另一种词条（结果 clamp 到上限，防止越界） */
    public BladeAffixes add(BladeAffixes other) {
        return new BladeAffixes(
            Math.min(MAX_LEVEL, this.chopper + other.chopper),
            Math.min(MAX_LEVEL, this.decapitator + other.decapitator),
            Math.min(MAX_LEVEL, this.dispossessor + other.dispossessor)
        );
    }
}
