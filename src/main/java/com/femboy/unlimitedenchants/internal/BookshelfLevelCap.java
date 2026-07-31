package com.femboy.unlimitedenchants.internal;

/**
 * Maps the enchanting table's bookshelf count to the highest enchantment level it's
 * allowed to produce, independent of any individual enchantment's own cost curve.
 * Without this, cheap, low-vanilla-max enchantments (Mending, Silk Touch, curses -
 * anything that vanilla caps at level 1) would satisfy their (plateaued) cost at
 * almost any power and jump straight to the configured max regardless of bookshelves.
 * <p>
 * Breakpoints: 0-15 bookshelves spans I-V, 15-20 spans V-XX, 20-30 spans XX up to
 * the configured maxLevel, and anything past 30 is fully open (capped only by
 * maxLevel itself). 15 bookshelves landing on level 5 deliberately mirrors vanilla's
 * own "15 bookshelves = max power" convention.
 */
public final class BookshelfLevelCap {
	private BookshelfLevelCap() {
	}

	public static int capFor(int bookshelves, int maxLevel) {
		int cap;
		if (bookshelves <= 15) {
			cap = lerp(1, 5, bookshelves, 0, 15);
		} else if (bookshelves <= 20) {
			cap = lerp(5, 20, bookshelves, 15, 20);
		} else if (bookshelves <= 30) {
			cap = lerp(20, maxLevel, bookshelves, 20, 30);
		} else {
			cap = maxLevel;
		}
		return Math.max(1, Math.min(cap, maxLevel));
	}

	private static int lerp(int fromValue, int toValue, int x, int fromX, int toX) {
		double t = (double) (x - fromX) / (toX - fromX);
		return (int) Math.round(fromValue + (toValue - fromValue) * t);
	}
}
