package dev.anvilcraft.addon.dearpluscelestialreforge.client.event;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.AutumniumIonocraftItem;
import dev.dubhe.anvilcraft.init.ModParticles;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 秋枫飘升机客户端处理器 — 飞行时喷出羽毛般的白至亮灰排气粒子。
 * 粒子位置跟随玩家身体模型（yBodyRot）旋转，与飘升机引擎位置一致。
 */
@EventBusSubscriber(modid = AnvilCraftDearPlusCelestialReforge.MOD_ID, value = Dist.CLIENT)
public class AutumniumIonocraftParticleHandler {
    /** 服务器同步的正在用飘升机飞行的玩家 entityId 集合（机制与飘升机背包一致） */
    private static final Set<Integer> SYNCED_FLYING_PLAYERS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** 由 {@code AutumniumIonocraftFlyingPacket} 在客户端调用，记录服务器同步的飞行状态 */
    public static void onFlyingSync(int playerId, boolean flying) {
        if (flying) {
            SYNCED_FLYING_PLAYERS.add(playerId);
        } else {
            SYNCED_FLYING_PLAYERS.remove(playerId);
        }
    }

    private static final double SIDE_OFFSET = 0.3;
    private static final double BACK_OFFSET = 0.45;
    private static final double Y_OFFSET = 1.1;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.isPaused()) return;
        ClientLevel level = minecraft.level;
        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer == null) return;
        boolean firstPerson = minecraft.options.getCameraType() == CameraType.FIRST_PERSON;

        for (Player player : level.players()) {
            // 自己的飘升机：第一人称时不显示粒子
            if (player == localPlayer && firstPerson) continue;
            if (player.isCreative() || player.isSpectator()) continue;

            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!(chest.getItem() instanceof AutumniumIonocraftItem) || AutumniumIonocraftItem.isBroken(chest)) continue;
            // 本地玩家用精确 abilities；远程玩家用服务器同步的精确状态（与飘升机背包一致）
            boolean flying = player == localPlayer
                ? player.getAbilities().flying
                : SYNCED_FLYING_PLAYERS.contains(player.getId());
            if (!flying) continue;

            spawnExhaustParticles(level, player, level.random);
        }
    }

    private static void spawnExhaustParticles(ClientLevel level, Player player, RandomSource random) {
        float yawRad = (float) Math.toRadians(player.yBodyRot);
        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);
        double backZ = -cosYaw;

        double[][] exhausts = {{SIDE_OFFSET, BACK_OFFSET}, {-SIDE_OFFSET, BACK_OFFSET}};

        for (double[] exhaust : exhausts) {
            double sideComp = exhaust[0];
            double backComp = exhaust[1];

            double worldX = player.getX() + sideComp * (-cosYaw) + backComp * sinYaw;
            double worldZ = player.getZ() + sideComp * (-sinYaw) + backComp * backZ;
            double worldY = player.getY() + Y_OFFSET;

            double velX = random.nextGaussian() * 0.02;
            double velY = -0.3 - random.nextFloat() * 0.3;
            double velZ = random.nextGaussian() * 0.02;

            level.addParticle(
                ModParticles.IONOCRAFT_BACKPACK_EXHAUST.get(),
                true,
                worldX + random.nextGaussian() * 0.08,
                worldY + random.nextGaussian() * 0.05,
                worldZ + random.nextGaussian() * 0.08,
                velX, velY, velZ
            );
        }
    }
}
