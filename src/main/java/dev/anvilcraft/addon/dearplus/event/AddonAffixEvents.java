package dev.anvilcraft.addon.dearplus.event;

import dev.anvilcraft.addon.dearplus.AnvilCraftDearPlus;
import dev.anvilcraft.addon.dearplus.init.AddonComponents;
import dev.anvilcraft.addon.dearplus.item.AutumniumIonocraftItem;
import dev.anvilcraft.addon.dearplus.item.RingedAutumniumBroadswordItem;
import dev.anvilcraft.addon.dearplus.item.property.component.AffixHelper;
import dev.anvilcraft.addon.dearplus.item.property.component.BladeAffixes;
import com.mojang.authlib.GameProfile;
import dev.anvilcraft.lib.v2.util.InventoryUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/**
 * 词条效果事件监听器。
 *
 * <ul>
 *     <li>破竹：攻击伤害 +{@code 0.5 + 0.5 * 等级}（等同锋利）；</li>
 *     <li>枭首：击杀生物按概率掉落头颅（等同斩首）；</li>
 *     <li>豪夺：击杀生物增加掉落（等同抢夺）；</li>
 *     <li>逐鹿：豪夺/枭首必定触发一次强运。</li>
 * </ul>
 */
@EventBusSubscriber(modid = AnvilCraftDearPlus.MOD_ID)
public class AddonAffixEvents {
    private AddonAffixEvents() {
    }

    /**
     * 偏安：每玩家 tick 把背包中偏安物品上已有的附魔吸收到
     * {@link AddonComponents#TRANQUIL_ENCHANTMENTS}，使其失效但保留显示。
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // 飘升机飞行状态同步（供其他客户端粒子渲染精确判断）
            AutumniumIonocraftItem.playerTick(serverPlayer);
            for (ItemStack stack : InventoryUtil.getItems(serverPlayer.getInventory(), AffixHelper::hasTranquil)) {
                AffixHelper.absorbEnchantments(stack);
            }
        }
    }

    /**
     * 破竹：主手武器持有破竹词条时，为玩家造成的伤害附加锋利等值的加伤。
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        // 仅玩家武器近战伤害触发词条（排除爆炸/魔法等间接伤害）
        if (!source.is(DamageTypes.PLAYER_ATTACK)) return;
        Entity direct = source.getDirectEntity();
        if (!(direct instanceof LivingEntity attacker)) return;
        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) return;
        BladeAffixes affixes = AffixHelper.getBladeAffixes(weapon);
        if (affixes.chopper() <= 0) return;
        // 原版锋利公式：0.5 + 0.5 * level
        event.setAmount(event.getAmount() + 0.5f * affixes.chopper() + 0.5f);
    }

    /**
     * 大环刀特性：所有秋枫大环刀对亡灵生物（僵尸/骷髅/凋灵等）造成的伤害翻倍。
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDamageUndead(LivingIncomingDamageEvent event) {
        if (!event.getEntity().getType().is(EntityTypeTags.UNDEAD)) return;
        DamageSource source = event.getSource();
        // 仅玩家武器近战伤害触发（排除爆炸/魔法等间接伤害）
        if (!source.is(DamageTypes.PLAYER_ATTACK)) return;
        Entity direct = source.getDirectEntity();
        if (!(direct instanceof LivingEntity attacker)) return;
        ItemStack weapon = attacker.getMainHandItem();
        // 与真实横扫口径一致：仅本 mod 大环刀物品（instanceof）触发，外部加标签物品不继承
        if (weapon.isEmpty() || !(weapon.getItem() instanceof RingedAutumniumBroadswordItem)) return;
        event.setAmount(event.getAmount() * 2.0f);
    }

    /**
     * 给掉落物增加 {@code extra} 个。当前掉落并入上限后，超出的部分拆成新的掉落物，
     * 避免物品数量超过堆叠上限。
     */
    private static void growDrop(ItemEntity drop, java.util.Collection<ItemEntity> drops, int extra) {
        if (extra <= 0) return;
        ItemStack stack = drop.getItem();
        int max = stack.getMaxStackSize();
        int canAdd = Math.min(extra, max - stack.getCount());
        if (canAdd > 0) stack.grow(canAdd);
        extra -= canAdd;
        while (extra > 0) {
            int amount = Math.min(extra, max);
            drops.add(new ItemEntity(
                drop.level(), drop.getX(), drop.getY(), drop.getZ(), stack.copyWithCount(amount)
            ));
            extra -= amount;
        }
    }

    /**
     * 枭首 + 豪夺：主手武器持有对应词条时，击杀生物掉落头颅并增加掉落数量。
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDrops(LivingDropsEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getDirectEntity() instanceof Player player)) return;
        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return;
        BladeAffixes affixes = AffixHelper.getBladeAffixes(weapon);
        if (affixes.isEmpty()) return;

        applyDispossessor(event, affixes.dispossessor());
        applyDecapitator(event, affixes.decapitator());
    }

    /** 豪夺：每个掉落物按概率额外掉落 1 ~ 等级 个。 */
    private static void applyDispossessor(LivingDropsEvent event, int level) {
        if (level <= 0) return;
        double chance = Math.min(0.5 + 0.01 * level, 1.0);
        for (ItemEntity drop : List.copyOf(event.getDrops())) {
            ItemStack stack = drop.getItem();
            if (stack.isEmpty()) continue;
            int extra = 0;
            if (event.getEntity().getRandom().nextFloat() < chance) {
                extra = 1 + event.getEntity().getRandom().nextInt(level);
            }
            if (extra > 0) growDrop(drop, event.getDrops(), extra);
        }
    }

    /** 枭首：击杀对应生物时按概率掉落头颅。 */
    private static void applyDecapitator(LivingDropsEvent event, int level) {
        if (level <= 0) return;
        LivingEntity entity = event.getEntity();
        ItemStack head = getHead(entity);
        if (head == null || head.isEmpty()) return;

        float chance;
        if (entity instanceof EnderDragon || entity instanceof Player) {
            chance = 1.0f;
        } else if (entity instanceof WitherSkeleton) {
            chance = 0.07f + 0.02f * level;
        } else if (entity instanceof Piglin) {
            chance = 0.01f + 0.01f * level;
        } else if (entity instanceof Zombie || entity instanceof Skeleton || entity instanceof Creeper) {
            chance = 0.01f + 0.01f * level;
        } else {
            return;
        }

        if (event.getEntity().getRandom().nextFloat() < chance) {
            event.getDrops().add(new ItemEntity(
                entity.level(), entity.getX(), entity.getY(), entity.getZ(), head
            ));
        }
    }

    /** 根据生物类型获取对应的头颅 */
    private static ItemStack getHead(LivingEntity entity) {
        if (entity instanceof WitherSkeleton) return new ItemStack(Items.WITHER_SKELETON_SKULL);
        if (entity instanceof Zombie) return new ItemStack(Items.ZOMBIE_HEAD);
        if (entity instanceof Skeleton) return new ItemStack(Items.SKELETON_SKULL);
        if (entity instanceof Creeper) return new ItemStack(Items.CREEPER_HEAD);
        if (entity instanceof Piglin) return new ItemStack(Items.PIGLIN_HEAD);
        if (entity instanceof EnderDragon) return new ItemStack(Items.DRAGON_HEAD);
        if (entity instanceof Player player) {
            // 带被击杀玩家资料的头（皮肤/名字），而非默认 Steve 头
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
            return head;
        }
        return ItemStack.EMPTY;
    }
}
