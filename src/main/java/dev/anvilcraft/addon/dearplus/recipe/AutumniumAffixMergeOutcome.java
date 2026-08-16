package dev.anvilcraft.addon.dearplus.recipe;

import com.mojang.serialization.MapCodec;
import dev.anvilcraft.addon.dearplus.init.AddonComponents;
import dev.anvilcraft.addon.dearplus.item.RingedAutumniumBroadswordItem;
import dev.anvilcraft.addon.dearplus.item.property.component.AffixHelper;
import dev.anvilcraft.addon.dearplus.item.property.component.BladeAffixes;
import dev.anvilcraft.lib.v2.recipe.cache.ItemCache;
import dev.anvilcraft.lib.v2.recipe.outcome.IRecipeOutcome;
import dev.anvilcraft.lib.v2.recipe.util.IRecipeResultOffsetBlock;
import dev.anvilcraft.lib.v2.recipe.util.InWorldRecipeContext;
import dev.dubhe.anvilcraft.init.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 升级配方的词条合并结果。
 *
 * <p>词条升级只与被配方消耗的金属粒相关：输入区域的金属粒分为「原始总量」与
 * 「消耗后剩余量」（ItemCache 中已被 shrink），两者之差即被消耗的金属粒，
 * 按种类统计为破竹/枭首/豪夺等级，与大环刀原有词条相加后生成新刀。</p>
 */
public class AutumniumAffixMergeOutcome implements IRecipeOutcome<AutumniumAffixMergeOutcome> {
    private final Vec3 inputOffset;
    private final Vec3 inputRange;
    private final Vec3 outputOffset;
    private final Item target;

    public AutumniumAffixMergeOutcome(Vec3 inputOffset, Vec3 inputRange, Vec3 outputOffset, Item target) {
        this.inputOffset = inputOffset;
        this.inputRange = inputRange;
        this.outputOffset = outputOffset;
        this.target = target;
    }

    @Override
    public void accept(InWorldRecipeContext context) {
        ServerLevel level = context.getLevel();
        Vec3 inputPos = context.getPos().add(this.inputOffset);
        AABB box = new AABB(inputPos, inputPos).inflate(
            this.inputRange.x(), this.inputRange.y(), this.inputRange.z()
        );

        // 1. 世界扫描：大环刀原有词条 + 金属粒原始总量（输出同步前实体仍在）
        // 一次只加工一把：刀词条只继承扫描到的第一把，多把刀不累加，避免词条异常叠加
        BladeAffixes base = BladeAffixes.EMPTY;
        ItemEnchantments storedEnchantments = ItemEnchantments.EMPTY;
        boolean bladeTaken = false;
        int ironOriginal = 0;
        int copperOriginal = 0;
        int goldOriginal = 0;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
            ItemStack stack = item.getItem();
            if (stack.getItem() instanceof RingedAutumniumBroadswordItem) {
                if (!bladeTaken) {
                    base = AffixHelper.getBladeAffixes(stack);
                    // 升级后仍是偏安物品：保留其被吸收的附魔
                    storedEnchantments = AffixHelper.getTranquilEnchantments(stack);
                    bladeTaken = true;
                }
            } else if (stack.is(Items.IRON_NUGGET)) {
                ironOriginal += stack.getCount();
            } else if (stack.is(ModItems.COPPER_NUGGET.get())) {
                copperOriginal += stack.getCount();
            } else if (stack.is(Items.GOLD_NUGGET)) {
                goldOriginal += stack.getCount();
            }
        }

        // 2. ItemCache 读取每种金属粒消耗后剩余量（已被配方 shrink）
        ItemCache cache = context.computeIfAbsent(ItemCache.ITEM_CACHE);
        int ironLeft = cache.getInput(s -> s.is(Items.IRON_NUGGET), inputPos, this.inputRange).getCount();
        int copperLeft = cache.getInput(s -> s.is(ModItems.COPPER_NUGGET.get()), inputPos, this.inputRange).getCount();
        int goldLeft = cache.getInput(s -> s.is(Items.GOLD_NUGGET), inputPos, this.inputRange).getCount();

        // 3. 消耗量 = 原始 - 剩余（只统计被消耗的金属粒）
        int chopper = Math.max(0, ironOriginal - ironLeft);
        int decapitator = Math.max(0, copperOriginal - copperLeft);
        int dispossessor = Math.max(0, goldOriginal - goldLeft);

        BladeAffixes merged = base.add(new BladeAffixes(chopper, decapitator, dispossessor));

        // 生成带词条的新刀，并保留其被吸收的附魔（升级后仍是偏安物品）
        ItemStack out = new ItemStack(this.target);
        AffixHelper.setBladeAffixes(out, merged);
        if (!storedEnchantments.isEmpty()) {
            out.set(AddonComponents.TRANQUIL_ENCHANTMENTS, storedEnchantments);
        }
        Vec3 outPos = context.getPos().add(this.outputOffset);
        // 与 anvillib 的 SpawnItem 一致：产物生成位置应用下方方块（如冲压平台）的
        // IRecipeResultOffsetBlock 偏移，使产物从平台前方出来。
        BlockPos outBlockPos = BlockPos.containing(outPos);
        BlockState outState = level.getBlockState(outBlockPos);
        if (outState.getBlock() instanceof IRecipeResultOffsetBlock offsetBlock) {
            outPos = outPos.add(offsetBlock.getOffset(level, outBlockPos, outState));
        }
        ItemEntity entity = new ItemEntity(level, outPos.x, outPos.y, outPos.z, out, 0, 0, 0);
        entity.setPickUpDelay(10);
        level.addFreshEntity(entity);
    }

    @Override
    public IRecipeOutcome.Type<AutumniumAffixMergeOutcome> getType() {
        return Type.INSTANCE;
    }

    /** 该结果仅由代码构造时注入，不经过 JSON/网络序列化，这里提供一个最小实现。 */
    public static class Type implements IRecipeOutcome.Type<AutumniumAffixMergeOutcome> {
        public static final Type INSTANCE = new Type();

        @Override
        public MapCodec<AutumniumAffixMergeOutcome> codec() {
            return MapCodec.unit(new AutumniumAffixMergeOutcome(Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, Items.AIR));
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AutumniumAffixMergeOutcome> streamCodec() {
            return StreamCodec.unit(new AutumniumAffixMergeOutcome(Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, Items.AIR));
        }
    }
}
