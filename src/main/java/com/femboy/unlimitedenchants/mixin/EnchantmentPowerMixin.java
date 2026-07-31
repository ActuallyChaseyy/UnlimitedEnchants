package com.femboy.unlimitedenchants.mixin;

import com.femboy.unlimitedenchants.config.ModConfig;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * EnchantmentHelper.getEnchantmentCost(...) clamps the bookshelf power it's given
 * to a hardcoded 15 before rolling a cost from it (the enchanting table itself can
 * detect up to 32 physical bookshelves - two full rings around the table - but the
 * cost formula throws away anything past 15). Replacing that constant with the
 * configured cap lets extra bookshelves keep raising the achievable cost (and so,
 * combined with EnchantmentCostMixin's plateau, the achievable level) instead of
 * capping out at vanilla's usual 15-bookshelf, 30-level ceiling.
 */
@Mixin(EnchantmentHelper.class)
public class EnchantmentPowerMixin {
	@ModifyConstant(method = "getEnchantmentCost", constant = @Constant(intValue = 15))
	private static int unlimitedEnchants$maxBookshelfPower(int original) {
		return ModConfig.getMaxBookshelves();
	}
}
