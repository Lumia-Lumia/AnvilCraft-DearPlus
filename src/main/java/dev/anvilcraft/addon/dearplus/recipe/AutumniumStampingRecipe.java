package dev.anvilcraft.addon.dearplus.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.addon.dearplus.init.ModRecipeSerializers;
import dev.anvilcraft.addon.dearplus.item.RingedAutumniumBroadswordItem;
import dev.anvilcraft.lib.v2.recipe.util.InWorldRecipeContext;
import dev.anvilcraft.lib.v2.util.predicate.ChanceItemStack;
import dev.anvilcraft.lib.v2.util.predicate.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.StampingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 秋枫三环刀 → 五环刀的物品冲压升级配方。
 *
 * <p>使用 AnvilCraft 的冲压平台（铁砧砸下），输入三环刀 + 2 个金属粒，
 * 生成五环刀并合并词条。</p>
 *
 * <p>必须继承 {@link StampingRecipe} 而非直接继承 {@code AbstractProcessRecipe}：
 * AnvilCraft 的 JEI {@code StampingCategory.draw} 会把配方强制转换为 {@code StampingRecipe}，
 * 若本类不是其子类，JEI 显示冲压配方时会抛 {@link ClassCastException}（anvilcraft 1.6.0+snapshot.2156）。</p>
 */
public class AutumniumStampingRecipe extends StampingRecipe {
    private static final Vec3 INPUT_OFFSET = new Vec3(0.0, -0.125, 0.0);
    private static final Vec3 INPUT_RANGE = new Vec3(0.75, 0.25, 0.75);
    private static final Vec3 OUTPUT_OFFSET = new Vec3(0.0, -0.25, 0.0);

    private final Item target;

    public AutumniumStampingRecipe(
        List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results, Item target
    ) {
        // 不把 results 放入 Property（resultItems 会被 AbstractProcessRecipe 自动生成 SpawnItem 白板输出）：
        // 输出由 AutumniumAffixMergeOutcome 生成带词条的新刀；JEI 展示用 getResultItems() 返回 target
        super(itemIngredients, List.of());
        this.target = target;
        // 产物生成位置与铁砧工艺冲压配方一致：
        // 原版用 hitBlockPos = pos.below()（平台），产物 = 平台中心上方 0.25
        // 本项目 context.getPos() 基于 pos（落点），故 outputOffset.y = -0.25（StampingRecipe 默认 -0.375 会差一格）
        this.getProperty().setItemOutputOffset(OUTPUT_OFFSET);
        this.getProperty().addOutcome(new AutumniumAffixMergeOutcome(INPUT_OFFSET, INPUT_RANGE, OUTPUT_OFFSET, target));
    }

    @Override
    public boolean matches(InWorldRecipeContext context, Level level) {
        // 一次只能加工一把：输入区域必须恰好有一把大环刀，多把时不匹配（不进入 super，避免消耗）
        Vec3 pos = context.getPos().add(INPUT_OFFSET);
        AABB box = new AABB(pos, pos).inflate(INPUT_RANGE.x(), INPUT_RANGE.y(), INPUT_RANGE.z());
        long blades = level.getEntitiesOfClass(ItemEntity.class, box).stream()
            .filter(e -> e.getItem().getItem() instanceof RingedAutumniumBroadswordItem)
            .count();
        if (blades != 1) return false;
        return super.matches(context, level);
    }

    @Override
    public ItemStack assemble(InWorldRecipeContext context, HolderLookup.Provider provider) {
        // 父类构造时 extraOutcomes 尚未进入 InWorldRecipe.outcomes（StampingRecipe 构造在 super 内
        // 生成 outcomes，之后 addOutcome 不会同步），此处显式执行词条合并 outcome：
        // 先 super.assemble 让 predicates 消耗输入，再执行 outcome（读取消耗后的 ItemCache 与世界扫描）
        ItemStack result = super.assemble(context, provider);
        this.getProperty().getExtraOutcomes().forEach(outcome -> outcome.acceptWithChance(context));
        return result;
    }

    @Override
    public List<ChanceItemStack> getResultItems() {
        // 展示用产物：target 刀（实际执行由 AutumniumAffixMergeOutcome 生成带词条的新刀）
        return List.of(ChanceItemStack.of(this.target, 1));
    }

    public Item getTarget() {
        return this.target;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<StampingRecipe> getSerializer() {
        // Serializer 运行时反序列化 target 并构造 AutumniumStampingRecipe（本类）
        return (RecipeSerializer<StampingRecipe>) (RecipeSerializer<?>) ModRecipeSerializers.AUTUMNIUM_STAMPING.get();
    }

    public static class Serializer implements RecipeSerializer<AutumniumStampingRecipe> {
        public static final MapCodec<AutumniumStampingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemIngredientPredicate.CODEC.listOf().optionalFieldOf("ingredients", List.of())
                .forGetter(AutumniumStampingRecipe::getInputItems),
            ChanceItemStack.CODEC.listOf().optionalFieldOf("results", List.of())
                .forGetter(AutumniumStampingRecipe::getResultItems),
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("target")
                .forGetter((AutumniumStampingRecipe recipe) -> BuiltInRegistries.ITEM.wrapAsHolder(recipe.getTarget()))
        ).apply(instance, (ingredients, results, target) ->
            new AutumniumStampingRecipe(ingredients, results, target.value())));

        public static final StreamCodec<RegistryFriendlyByteBuf, AutumniumStampingRecipe> STREAM_CODEC =
            StreamCodec.of(
                (buf, recipe) -> {
                    ItemIngredientPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.getInputItems());
                    ChanceItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.getResultItems());
                    buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.getTarget()));
                },
                buf -> {
                    List<ItemIngredientPredicate> ingredients = ItemIngredientPredicate.STREAM_CODEC
                        .apply(ByteBufCodecs.list()).decode(buf);
                    List<ChanceItemStack> results = ChanceItemStack.STREAM_CODEC
                        .apply(ByteBufCodecs.list()).decode(buf);
                    Item target = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                    return new AutumniumStampingRecipe(ingredients, results, target);
                }
            );

        @Override
        public MapCodec<AutumniumStampingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AutumniumStampingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
