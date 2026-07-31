package com.femboy.unlimitedenchants.internal;

import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Remembers each enchantment's original, data-defined max level before it gets
 * overwritten with the configured cap. Used to keep the enchanting table's cost
 * curve (tuned for vanilla's small levels) from blocking the new, higher levels.
 * Keyed by identity, not equals/hashCode, since definitions are effectively
 * singletons and the record's generated equals/hashCode would be far more
 * expensive to compute on every enchanting table roll.
 * <p>
 * Lives outside the mixin package deliberately - Mixin reserves that package for
 * mixin classes only and refuses to load anything else from it.
 */
public final class VanillaMaxLevelCache {
	private static final Map<Enchantment.EnchantmentDefinition, Integer> ORIGINAL_MAX_LEVELS =
			Collections.synchronizedMap(new IdentityHashMap<>());

	private VanillaMaxLevelCache() {
	}

	public static void recordOriginal(Enchantment.EnchantmentDefinition definition, int originalMaxLevel) {
		ORIGINAL_MAX_LEVELS.putIfAbsent(definition, originalMaxLevel);
	}

	public static int clampToOriginal(Enchantment.EnchantmentDefinition definition, int level) {
		Integer original = ORIGINAL_MAX_LEVELS.get(definition);
		return original == null ? level : Math.min(level, original);
	}
}
