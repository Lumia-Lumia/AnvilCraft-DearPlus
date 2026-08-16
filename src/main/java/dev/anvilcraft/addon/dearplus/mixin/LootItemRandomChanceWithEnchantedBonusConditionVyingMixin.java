package dev.anvilcraft.addon.dearplus.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.anvilcraft.addon.dearplus.item.property.component.AffixHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 逐鹿：提升「稀有掉落物」的掉落概率。
 *
 * <p>怪物战利品表的稀有掉落（如僵尸的铁锭、胡萝卜）通过
 * {@code random_chance_with_enchanted_bonus} 条件判定。持有「逐鹿」等级 N 的武器击杀时，
 * 稀有掉落概率按 N 倍放大（最高 100%）。</p>
 */
@Mixin(LootItemRandomChanceWithEnchantedBonusCondition.class)
public abstract class LootItemRandomChanceWithEnchantedBonusConditionVyingMixin {
    @ModifyReturnValue(method = "test", at = @At("RETURN"))
    private boolean vyingBoostRareDrop(boolean original, LootContext context) {
        if (original) return true;
        Entity attacker = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
        if (!(attacker instanceof LivingEntity living)) return original;
        ItemStack weapon = living.getMainHandItem();
        int vyingLevel = AffixHelper.getVyingLevel(weapon);
        if (vyingLevel <= 0) return original;
        var cond = (LootItemRandomChanceWithEnchantedBonusCondition) (Object) this;
        int looting = EnchantmentHelper.getEnchantmentLevel(cond.enchantment(), living);
        float p = looting > 0 ? cond.enchantedChance().calculate(looting) : cond.unenchantedChance();
        // 此处已条件于「原版未通过」（original=false 由上方 early return 处理），
        // 二次掷骰的条件概率为 L/10，最终概率 = p + (1-p)×L/10，与设计公式一致。
        // P=0 时逐鹿不产生原本不存在的稀有掉落。
        if (p <= 0.0f) return false;
        return context.getRandom().nextFloat() < vyingLevel / 10.0f;
    }
}
