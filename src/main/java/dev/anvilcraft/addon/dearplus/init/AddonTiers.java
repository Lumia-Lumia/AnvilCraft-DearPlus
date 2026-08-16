package dev.anvilcraft.addon.dearplus.init;

import com.google.common.base.Suppliers;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * 秋枫工具等级。
 *
 * <p>耐久 254（非 255，避免与刀词条上限 MAX_LEVEL=255 撞车混淆），
 * 挖掘等级为铁，不可用任何材料在铁砧/工作台中修复。</p>
 */
public enum AddonTiers implements Tier {
    AUTUMNIUM(
        254,
        6.0f,
        2.0f,
        14,
        () -> Ingredient.EMPTY,
        BlockTags.INCORRECT_FOR_IRON_TOOL
    ),
    ;

    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;
    private final TagKey<Block> incorrectBlockTags;

    AddonTiers(
        int uses,
        float speed,
        float damage,
        int enchantmentValue,
        Supplier<Ingredient> supplier,
        TagKey<Block> incorrectBlockTags
    ) {
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = Suppliers.memoize(supplier::get);
        this.incorrectBlockTags = incorrectBlockTags;
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.damage;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return this.incorrectBlockTags;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
