package com.femboy.unlimitedenchants.mixin;

import com.femboy.unlimitedenchants.config.ModConfig;
import com.femboy.unlimitedenchants.internal.LootGenerationContext;
import com.femboy.unlimitedenchants.internal.VanillaMaxLevelCache;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Enchantment.getMaxLevel() and every other max-level check (the enchanting table,
 * anvil combining, /enchant, loot) all read from this single accessor, so overriding
 * it here raises the ceiling everywhere at once instead of per call site.
 * While LootGenerationContext is active (a loot table roll, or a mob's
 * default-equipment enchantment roll) and excludeLootFromBoost is enabled, the
 * override is skipped so loot keeps using vanilla's real max levels; villager
 * trades never enter that context, so they're unaffected either way.
 */
@Mixin(Enchantment.EnchantmentDefinition.class)
public class EnchantmentMaxLevelMixin {
	@Inject(method = "maxLevel", at = @At("RETURN"), cancellable = true)
	private void unlimitedEnchants$overrideMaxLevel(CallbackInfoReturnable<Integer> cir) {
		Enchantment.EnchantmentDefinition self = (Enchantment.EnchantmentDefinition) (Object) this;
		VanillaMaxLevelCache.recordOriginal(self, cir.getReturnValue());

		if (ModConfig.isExcludeLootFromBoost() && LootGenerationContext.isActive()) {
			return;
		}

		cir.setReturnValue(ModConfig.getMaxLevel());
	}
}
