package dev.anvilcraft.addon.dearpluscelestialreforge.network;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.client.event.AutumniumIonocraftParticleHandler;
import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

/**
 * 秋枫飘升机飞行状态同步包（Server → Client）。
 *
 * <p>当玩家开始/停止用飘升机飞行时通知周边客户端，供排气粒子渲染精确判断。
 * 机制与飘升机背包 {@code IonocraftBackpackFlyingPacket} 一致：远程玩家的
 * {@code getAbilities().flying} 不可靠，需服务端同步精确状态。</p>
 */
public record AutumniumIonocraftFlyingPacket(int playerId, boolean flying) implements IClientboundPacket {
    public static final Type<AutumniumIonocraftFlyingPacket> TYPE =
        IPacket.type(AnvilCraftDearPlusCelestialReforge.of("autumnium_ionocraft_flying"));

    public static final StreamCodec<ByteBuf, AutumniumIonocraftFlyingPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, AutumniumIonocraftFlyingPacket::playerId,
        ByteBufCodecs.BOOL, AutumniumIonocraftFlyingPacket::flying,
        AutumniumIonocraftFlyingPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        AutumniumIonocraftParticleHandler.onFlyingSync(this.playerId, this.flying);
    }
}
