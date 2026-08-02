package com.femboy.unlimitedenchants.mixin;

import com.femboy.unlimitedenchants.config.ModConfig;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * AnvilMenu.createResult() compares the accumulated cost against a hardcoded 40 in
 * two places - once for the "repairing with materials only" case, which just clamps
 * the cost down to 39 instead of blocking, and once for the general case, which
 * empties the result slot entirely ("Too Expensive!") - plus the literal 39 used as
 * that clamp target. A third, unrelated "40" earlier in the method forcibly triggers
 * the block when the second input slot holds a stack of more than one item; that one
 * is intentionally left alone since it's a separate anti-exploit rule, not part of
 * the cost cap this mod is meant to configure.
 * When noAnvilLimit is on, both thresholds become Integer.MAX_VALUE, which cost can
 * never realistically reach, so neither check ever fires and nothing gets blocked or
 * clamped. When it's off, they use maxAnvilCost (+1 for the ">=" comparisons) instead
 * of vanilla's fixed 39/40, so the limit still applies but at a configurable level.
 * Separately, just before any of that, the method clamps the raw accumulated cost
 * into {@code cost} via {@code Mth.clamp(base + added, 0, Integer.MAX_VALUE)} - with
 * the block disabled, that upper bound is the only thing left standing between a
 * heavily-repaired item (vanilla's "prior work penalty" roughly doubles cost per
 * prior anvil use) and an effectively unbounded charge. Replacing it with
 * maxUnlimitedAnvilCost gives noAnvilLimit a sane ceiling of its own instead of
 * Integer.MAX_VALUE.
 */
@Mixin(AnvilMenu.class)
public class AnvilCostLimitMixin {
	@ModifyConstant(method = "createResult", constant = @Constant(longValue = Integer.MAX_VALUE))
	private long unlimitedEnchants$unlimitedCostCeiling(long original) {
		return ModConfig.isNoAnvilLimit() ? ModConfig.getMaxUnlimitedAnvilCost() : original;
	}

	@ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 1))
	private int unlimitedEnchants$repairOnlyThreshold(int original) {
		return ModConfig.isNoAnvilLimit() ? Integer.MAX_VALUE : ModConfig.getMaxAnvilCost() + 1;
	}

	@ModifyConstant(method = "createResult", constant = @Constant(intValue = 39, ordinal = 0))
	private int unlimitedEnchants$repairOnlyClampValue(int original) {
		return ModConfig.getMaxAnvilCost();
	}

	@ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 2))
	private int unlimitedEnchants$blockThreshold(int original) {
		return ModConfig.isNoAnvilLimit() ? Integer.MAX_VALUE : ModConfig.getMaxAnvilCost() + 1;
	}
}
