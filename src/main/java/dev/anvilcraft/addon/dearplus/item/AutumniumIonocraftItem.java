package dev.anvilcraft.addon.dearplus.item;

import com.mojang.datafixers.util.Unit;
import dev.anvilcraft.addon.dearplus.AnvilCraftDearPlus;
import dev.anvilcraft.addon.dearplus.init.AddonComponents;
import dev.anvilcraft.addon.dearplus.network.AutumniumIonocraftFlyingPacket;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 秋枫飘升机。
 *
 * <p>持有词条「偏安」。穿在身上时提供 +4 护甲，并像飘升机背包一样为玩家启用创造飞行。
 * 飞行时每秒消耗 1 点耐久；耐久降到 1 时失效并显示损毁纹理（类似鞘翅），
 * 但损耗会在耐久 1 处停止，不会真正损坏消失。</p>
 */
public class AutumniumIonocraftItem extends ArmorItem {
    private static final ResourceLocation CREATIVE_FLIGHT_ID = AnvilCraftDearPlus.of("creative_flight");
    private static final AttributeModifier CREATIVE_FLIGHT = new AttributeModifier(
        CREATIVE_FLIGHT_ID, 1, AttributeModifier.Operation.ADD_VALUE
    );

    private static final ArmorMaterial AUTUMNIUM_MATERIAL = new ArmorMaterial(
        Map.of(Type.CHESTPLATE, 4),
        0,
        SoundEvents.ARMOR_EQUIP_IRON,
        () -> Ingredient.EMPTY,
        List.of(new ArmorMaterial.Layer(AnvilCraftDearPlus.of("autumnium_ionocraft"))),
        0.0f,
        0.0f
    );

    public AutumniumIonocraftItem(Properties properties) {
        super(
            Holder.direct(AUTUMNIUM_MATERIAL),
            Type.CHESTPLATE,
            properties
                .durability(432)
                .component(AddonComponents.TRANQUIL, Unit.INSTANCE)
        );
    }

    /** 飘升机是否已损坏（耐久降到 1），此时飞行失效并显示损毁纹理 */
    public static boolean isBroken(ItemStack stack) {
        return stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    @Override
    @SuppressWarnings("removal")
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(
                LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original
            ) {
                // 临时外观：穿戴时复用飘升机背包的独立模型
                return dev.dubhe.anvilcraft.client.init.ModModelLayers.getIonocraftBackpackModel();
            }
        });
        ItemProperties.register(
            this,
            ResourceLocation.withDefaultNamespace("broken"),
            (stack, level, entity, seed) -> isBroken(stack) ? 1.0F : 0.0F
        );
    }

    /**
     * 穿戴时复用飘升机背包的盔甲贴图（临时外观），损坏时显示背包关闭贴图。
     */
    @Override
    public @Nullable ResourceLocation getArmorTexture(
        ItemStack stack, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.EquipmentSlot slot,
        net.minecraft.world.item.ArmorMaterial.Layer layer, boolean innerModel
    ) {
        return ResourceLocation.fromNamespaceAndPath(
            AnvilCraftDearPlus.MOD_ID, "textures/entity/equipment/autumnium_ionocraft.png"
        );
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (entity instanceof Player player) {
            tickFlight(stack, level, player);
        }
    }

    private static void tickFlight(ItemStack stack, Level level, Player player) {
        boolean equipped = player.getItemBySlot(EquipmentSlot.CHEST) == stack;
        // 与飘升机背包一致：只有胸甲槽穿戴的那件管理飞行 modifier，背包里的其他件不参与
        if (!equipped) return;
        boolean canFly = !isBroken(stack)
            && !player.isCreative()
            && !player.isSpectator();

        AttributeInstance instance = player.getAttributes().getInstance(NeoForgeMod.CREATIVE_FLIGHT);
        if (canFly) {
            if (instance != null && !instance.hasModifier(CREATIVE_FLIGHT_ID)) {
                instance.addTransientModifier(CREATIVE_FLIGHT);
            }
            // 飞行时每秒消耗 1 点耐久（类似鞘翅）
            if (player.getAbilities().flying && !level.isClientSide && level.getGameTime() % 20 == 0) {
                stack.hurtAndBreak(1, player, EquipmentSlot.CHEST);
            }
        } else if (instance != null && instance.hasModifier(CREATIVE_FLIGHT_ID)) {
            instance.removeModifier(CREATIVE_FLIGHT);
        }
    }

    /** 服务端：追踪玩家飘升机飞行状态，变化时广播到周边客户端（供排气粒子判断） */
    private static final Map<UUID, Boolean> FLYING_TRACKER = new HashMap<>();

    /**
     * 服务端：按玩家 tick 监测飘升机飞行状态，状态变化时广播到周边客户端。
     * 机制与飘升机背包 {@code IonocraftBackpackItem.playerTick} 一致：
     * 远程玩家的 {@code getAbilities().flying} 不可靠，需同步精确状态供粒子渲染。
     */
    public static void playerTick(ServerPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean nowFlying = !chest.isEmpty()
            && chest.getItem() instanceof AutumniumIonocraftItem
            && !isBroken(chest)
            && player.getAbilities().flying
            && !player.isCreative()
            && !player.isSpectator();
        Boolean prevFlying = FLYING_TRACKER.put(player.getUUID(), nowFlying);
        if (prevFlying == null || prevFlying != nowFlying) {
            PacketDistributor.sendToPlayersTrackingEntity(
                player,
                new AutumniumIonocraftFlyingPacket(player.getId(), nowFlying)
            );
        }
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        // 损耗在耐久 1 处停止，飘升机不会真正损坏消失
        int remaining = stack.getMaxDamage() - 1 - stack.getDamageValue();
        if (remaining <= 0) return 0;
        return Math.min(amount, remaining);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }
}
