package dev.anvilcraft.addon.dearplus.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.addon.dearplus.init.ModRecipeSerializers;
import dev.anvilcraft.addon.dearplus.item.property.component.AffixHelper;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.anvilcraft.addon.dearplus.item.property.component.BladeAffixes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

/**
 * 秋枫三环刀的工作台合成配方。
 *
 * <p>配方中使用铁粒/铜粒/金粒，合成出的三环刀会带有对应等级的
 * 破竹/枭首/豪夺词条（每种金属粒 +1 级）。</p>
 */
public class RingedAutumniumBroadswordCraftingRecipe extends ShapedRecipe {
    public RingedAutumniumBroadswordCraftingRecipe(
        String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification
    ) {
        super(group, category, pattern, result, showNotification);
    }

    public ItemStack autumniumResult() {
        return this.getResultItem(null);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.AUTUMNIUM_RING_BLADE_CRAFTING.get();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries);
        int chopper = 0;
        int decapitator = 0;
        int dispossessor = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(Items.IRON_NUGGET)) {
                chopper++;
            } else if (stack.is(ModItems.COPPER_NUGGET.get())) {
                decapitator++;
            } else if (stack.is(Items.GOLD_NUGGET)) {
                dispossessor++;
            }
        }
        AffixHelper.setBladeAffixes(result, new BladeAffixes(chopper, decapitator, dispossessor));
        return result;
    }

    public static class Serializer implements RecipeSerializer<RingedAutumniumBroadswordCraftingRecipe> {
        public static final MapCodec<RingedAutumniumBroadswordCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
            ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(RingedAutumniumBroadswordCraftingRecipe::autumniumResult),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification)
        ).apply(instance, RingedAutumniumBroadswordCraftingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RingedAutumniumBroadswordCraftingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ShapedRecipe::getGroup,
                ByteBufCodecs.fromCodec(CraftingBookCategory.CODEC), ShapedRecipe::category,
                ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
                ItemStack.STREAM_CODEC, RingedAutumniumBroadswordCraftingRecipe::autumniumResult,
                ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
                RingedAutumniumBroadswordCraftingRecipe::new
            );

        @Override
        public MapCodec<RingedAutumniumBroadswordCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RingedAutumniumBroadswordCraftingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
