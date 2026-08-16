package dev.anvilcraft.addon.dearplus.network;

import dev.anvilcraft.addon.dearplus.AnvilCraftDearPlus;
import dev.anvilcraft.addon.dearplus.util.TrueSweepHelper;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.anvilcraft.lib.v2.network.packet.IServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

/**
 * 客户端攻击挥空（点空气）时通知服务端的包，用于区分「攻击挥刀」与交互/丢物品的挥刀。
 */
public record AutumniumSwingPacket() implements IServerboundPacket {
    public static final Type<AutumniumSwingPacket> TYPE =
        IPacket.type(AnvilCraftDearPlus.of("autumnium_swing"));
    public static final StreamCodec<ByteBuf, AutumniumSwingPacket> STREAM_CODEC =
        StreamCodec.unit(new AutumniumSwingPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleOnServer(Player player) {
        TrueSweepHelper.markAttackSwing(player);
    }
}
