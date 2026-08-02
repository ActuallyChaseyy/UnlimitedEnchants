package com.femboy.unlimitedenchants.mixin;

import com.femboy.unlimitedenchants.internal.VanillaMaxLevelCache;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The enchanting table picks a level by scanning from getMaxLevel() down to 1 and
 * taking the first one whose min/max cost brackets the table's rolled power. Those
 * cost curves are still the original per-enchantment formulas (e.g. Sharpness costs
 * 11 "power" per level above the first), so left alone they'd only ever satisfy the
 * table's ~1-30 power range around vanilla's old levels, never the new, much higher
 * ones - the ceiling would go up but the table would never actually reach it.
 * Clamping the level fed into the cost formula to the enchantment's original max
 * level makes cost plateau past that point, so whenever the table's power would have
 * earned vanilla's top level, it now earns the configured top level instead - the
 * scan starts from the top, so the highest level sharing that plateaued cost wins.
 */
@Mixin(Enchantment.class)
public abstract class EnchantmentCostMixin {
	@Shadow
	public abstract Enchantment.EnchantmentDefinition definition();

	@Inject(method = "getMinCost", at = @At("HEAD"), cancellable = true)
	private void unlimitedEnchants$clampMinCost(int level, CallbackInfoReturnable<Integer> cir) {
		int clampedLevel = VanillaMaxLevelCache.clampToOriginal(this.definition(), level);
		cir.setReturnValue(this.definition().minCost().calculate(clampedLevel));
	}

	@Inject(method = "getMaxCost", at = @At("HEAD"), cancellable = true)
	private void unlimitedEnchants$clampMaxCost(int level, CallbackInfoReturnable<Integer> cir) {
		int clampedLevel = VanillaMaxLevelCache.clampToOriginal(this.definition(), level);
		cir.setReturnValue(this.definition().maxCost().calculate(clampedLevel));
	}
}
