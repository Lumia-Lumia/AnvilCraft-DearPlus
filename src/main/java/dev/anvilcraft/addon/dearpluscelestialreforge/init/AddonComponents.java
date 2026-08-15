package dev.anvilcraft.addon.dearpluscelestialreforge.init;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.property.component.BladeAffixes;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

/**
 * 附属的词条（affix）数据组件注册表。
 *
 * <p>词条通过物品数据组件绑定在物品上：</p>
 * <ul>
 *     <li>{@link #TRANQUIL}（偏安）：持有该组件的物品无法附魔/祛魔，所有附魔失效；</li>
 *     <li>{@link #VYING}（逐鹿）：附魔无需经验，必定触发一次强运；</li>
 *     <li>{@link #BLADE_AFFIXES}（破竹/枭首/豪夺）：刀类武器的锋利/斩首/抢夺等级；</li>
 *     <li>{@link #TRANQUIL_ENCHANTMENTS}：偏安物品被吸收（无效化）的附魔。</li>
 * </ul>
 */
public class AddonComponents {
    private static final DeferredRegister<DataComponentType<?>> DR = DeferredRegister.create(
        Registries.DATA_COMPONENT_TYPE, AnvilCraftDearPlusCelestialReforge.MOD_ID
    );

    /** 偏安 */
    public static final DataComponentType<Unit> TRANQUIL = registerEmpty("tranquil");
    /** 逐鹿（等级：免除经验消耗，触发对应次数的强运；上限 10） */
    public static final DataComponentType<Integer> VYING = register(
        "vying",
        b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT)
    );
    /** 破竹 / 枭首 / 豪夺 等级 */
    public static final DataComponentType<BladeAffixes> BLADE_AFFIXES = register(
        "blade_affixes",
        b -> b.persistent(BladeAffixes.CODEC.codec()).networkSynchronized(BladeAffixes.STREAM_CODEC)
    );
    /** 偏安物品被吸收（无效化）的附魔 */
    public static final DataComponentType<ItemEnchantments> TRANQUIL_ENCHANTMENTS = register(
        "tranquil_enchantments",
        b -> b.persistent(ItemEnchantments.CODEC).networkSynchronized(ItemEnchantments.STREAM_CODEC)
    );

    private static <T> DataComponentType<T> register(String name, Consumer<DataComponentType.Builder<T>> customizer) {
        var builder = DataComponentType.<T>builder();
        customizer.accept(builder);
        var componentType = builder.build();
        DR.register(name, () -> componentType);
        return componentType;
    }

    private static DataComponentType<Unit> registerEmpty(String name) {
        return register(
            name,
            b -> b.persistent(Codec.EMPTY.codec()).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
        );
    }

    public static void register(IEventBus bus) {
        DR.register(bus);
    }
}
