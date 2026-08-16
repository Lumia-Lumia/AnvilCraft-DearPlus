package dev.anvilcraft.addon.dearplus.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.addon.dearplus.init.ModRecipeSerializers;
import dev.anvilcraft.addon.dearplus.item.RingedAutumniumBroadswordItem;
import dev.anvilcraft.lib.v2.recipe.util.InWorldRecipeContext;
import dev.anvilcraft.lib.v2.util.predicate.BlockStatePredicate;
import dev.anvilcraft.lib.v2.util.predicate.ChanceItemStack;
import dev.anvilcraft.lib.v2.util.predicate.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.block.BurningHeaterBlock;
import dev.dubhe.anvilcraft.block.HeaterBlock;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.recipe.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.AbstractProcessRecipe;
import dev.dubhe.anvilcraft.recipe.component.HasCauldronSimple;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 秋枫五环刀 → 七环刀的高温熔炼升级配方。
 *
 * <p>使用 AnvilCraft 的高温熔炼机制：铁砧砸击底部有工作的电加热器或燃烧加热器的
 * 炼药锅/鱼缸，输入五环刀 + 2 个金属粒，生成七环刀并合并词条。</p>
 */
public class AutumniumHeatingRecipe extends AbstractProcessRecipe<AutumniumHeatingRecipe> {
    private static final Vec3 INPUT_OFFSET = new Vec3(0.0, -0.375, 0.0);
    private static final Vec3 INPUT_RANGE = new Vec3(0.75, 0.75, 0.75);

    private final Item target;

    public AutumniumHeatingRecipe(
        List<ItemIngredientPredicate> itemIngredients,
        List<ChanceItemStack> results,
        HasCauldronSimple hasCauldron,
        Item target
    ) {
        super(createProperty(itemIngredients, results, hasCauldron, target));
        this.target = target;
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

    private static Property createProperty(
        List<ItemIngredientPredicate> itemIngredients,
        List<ChanceItemStack> results,
        HasCauldronSimple hasCauldron,
        Item target
    ) {
        Property property = new Property()
            .setItemInputOffset(new Vec3(0.0, -0.375, 0.0))
            .setItemInputRange(new Vec3(0.75, 0.75, 0.75))
            .setInputItems(itemIngredients)
            .setItemOutputOffset(new Vec3(0.0, -0.75, 0.0))
            // 不生成 SpawnItem 白板输出：输出由 AutumniumAffixMergeOutcome 生成带词条的新刀
            .setCauldronOffset(new Vec3i(0, -1, 0))
            .setHasCauldron(hasCauldron)
            .setBlockInputOffset(new Vec3i(0, -2, 0))
            .setInputBlocks(BlockStatePredicate.builder()
                .of(ModBlocks.HEATER.get(), ModBlocks.BURNING_HEATER.get())
                .with(HeaterBlock.OVERLOAD, false)
                .or()
                .with(BurningHeaterBlock.LEVEL, 2)
                .build());
        property.addOutcome(new AutumniumAffixMergeOutcome(
            new Vec3(0.0, -0.375, 0.0), new Vec3(0.75, 0.75, 0.75), new Vec3(0.0, -0.75, 0.0), target
        ));
        return property;
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
    public RecipeSerializer<AutumniumHeatingRecipe> getSerializer() {
        return ModRecipeSerializers.AUTUMNIUM_HEATING.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeType<AutumniumHeatingRecipe> getType() {
        return (RecipeType<AutumniumHeatingRecipe>) (RecipeType<?>) ModRecipeTypes.SUPER_HEATING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<AutumniumHeatingRecipe> {
        public static final MapCodec<AutumniumHeatingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemIngredientPredicate.CODEC.listOf().optionalFieldOf("ingredients", List.of())
                .forGetter(AutumniumHeatingRecipe::getInputItems),
            ChanceItemStack.CODEC.listOf().optionalFieldOf("results", List.of())
                .forGetter(AutumniumHeatingRecipe::getResultItems),
            HasCauldronSimple.CODEC.fieldOf("hasCauldron")
                .orElse(HasCauldronSimple.empty().build())
                .forGetter(AutumniumHeatingRecipe::getHasCauldron),
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("target")
                .forGetter((AutumniumHeatingRecipe recipe) -> BuiltInRegistries.ITEM.wrapAsHolder(recipe.getTarget()))
        ).apply(instance, (ingredients, results, hasCauldron, target) ->
            new AutumniumHeatingRecipe(ingredients, results, hasCauldron, target.value())));

        public static final StreamCodec<RegistryFriendlyByteBuf, AutumniumHeatingRecipe> STREAM_CODEC =
            StreamCodec.of(
                (buf, recipe) -> {
                    ItemIngredientPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.getInputItems());
                    ChanceItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.getResultItems());
                    HasCauldronSimple.STREAM_CODEC.encode(buf, recipe.getHasCauldron());
                    buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.getTarget()));
                },
                buf -> {
                    List<ItemIngredientPredicate> ingredients = ItemIngredientPredicate.STREAM_CODEC
                        .apply(ByteBufCodecs.list()).decode(buf);
                    List<ChanceItemStack> results = ChanceItemStack.STREAM_CODEC
                        .apply(ByteBufCodecs.list()).decode(buf);
                    HasCauldronSimple hasCauldron = HasCauldronSimple.STREAM_CODEC.decode(buf);
                    Item target = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                    return new AutumniumHeatingRecipe(ingredients, results, hasCauldron, target);
                }
            );

        @Override
        public MapCodec<AutumniumHeatingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AutumniumHeatingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
