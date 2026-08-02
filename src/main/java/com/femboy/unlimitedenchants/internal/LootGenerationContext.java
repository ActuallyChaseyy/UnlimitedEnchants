package com.femboy.unlimitedenchants.internal;

/**
 * Tracks whether the current thread is inside vanilla loot generation (a loot
 * table roll, or a mob's default-equipment enchantment roll), so
 * EnchantmentMaxLevelMixin can fall back to each enchantment's real, data-defined
 * max level instead of the configured boosted one while it's active.
 * Villager trades apply their item functions directly (VillagerTrade.getOffer())
 * without ever calling through LootTable, so they never enter this scope and keep
 * using the boosted range - exactly the "excluding villager trades" carve-out this
 * exists for.
 * A depth counter rather than a boolean, since loot tables can reference other loot
 * tables (nested rolls) - the outer roll's exit must not clear suppression while an
 * inner roll is still in progress.
 */
public final class LootGenerationContext {
	private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

	private LootGenerationContext() {
	}

	public static void enter() {
		DEPTH.set(DEPTH.get() + 1);
	}

	public static void exit() {
		DEPTH.set(Math.max(0, DEPTH.get() - 1));
	}

	public static boolean isActive() {
		return DEPTH.get() > 0;
	}
}
