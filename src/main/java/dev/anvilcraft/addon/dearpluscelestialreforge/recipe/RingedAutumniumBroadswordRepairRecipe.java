package dev.anvilcraft.addon.dearpluscelestialreforge.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.ModRecipeSerializers;
import dev.anvilcraft.addon.dearpluscelestialreforge.item.RingedAutumniumBroadswordItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/**
 * 秋枫大环刀的工作台修复配方。
 *
 * <p>大环刀 + 1 个秋枫合金块（无序配方）→ 满耐久的同档大环刀，并保留破竹/枭首/豪夺词条。</p>
 */
public class RingedAutumniumBroadswordRepairRecipe extends ShapelessRecipe {
    public RingedAutumniumBroadswordRepairRecipe(
        String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients
    ) {
        super(group, category, result, ingredients);
    }

    public ItemStack autumniumResult() {
        return this.getResultItem(null);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.AUTUMNIUM_RING_BLADE_REPAIR.get();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        // 找出输入中的大环刀，复制其物品与词条，并恢复满耐久
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof RingedAutumniumBroadswordItem) {
                ItemStack result = stack.copy();
                result.setDamageValue(0);
                return result;
            }
        }
        return super.assemble(input, registries);
    }

    public static class Serializer implements RecipeSerializer<RingedAutumniumBroadswordRepairRecipe> {
        public static final MapCodec<RingedAutumniumBroadswordRepairRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(RingedAutumniumBroadswordRepairRecipe::autumniumResult),
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(
                list -> {
                    Ingredient[] arr = list.stream().filter(ing -> !ing.isEmpty()).toArray(Ingredient[]::new);
                    if (arr.length == 0) {
                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                    }
                    if (arr.length > 9) {
                        return DataResult.error(() -> "Too many ingredients for shapeless recipe");
                    }
                    return DataResult.success(NonNullList.of(Ingredient.EMPTY, arr));
                },
                DataResult::success
            ).forGetter(ShapelessRecipe::getIngredients)
        ).apply(instance, RingedAutumniumBroadswordRepairRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RingedAutumniumBroadswordRepairRecipe> STREAM_CODEC =
            StreamCodec.of(
                (buf, recipe) -> {
                    buf.writeUtf(recipe.getGroup());
                    buf.writeEnum(recipe.category());
                    ItemStack.STREAM_CODEC.encode(buf, recipe.autumniumResult());
                    NonNullList<Ingredient> ingredients = recipe.getIngredients();
                    buf.writeVarInt(ingredients.size());
                    for (Ingredient ingredient : ingredients) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
                    }
                },
                buf -> {
                    String group = buf.readUtf();
                    CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
                    ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                    int size = buf.readVarInt();
                    NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
                    for (int i = 0; i < size; i++) {
                        ingredients.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    }
                    return new RingedAutumniumBroadswordRepairRecipe(group, category, result, ingredients);
                }
            );

        @Override
        public MapCodec<RingedAutumniumBroadswordRepairRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RingedAutumniumBroadswordRepairRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
