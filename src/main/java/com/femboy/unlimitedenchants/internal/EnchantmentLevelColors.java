package com.femboy.unlimitedenchants.internal;

import net.minecraft.network.chat.TextColor;

/**
 * Levels above vanilla's normal 1-5 range get a distinct color band so a glance at
 * the tooltip tells you roughly how enchanted something is, independent of reading
 * the roman numeral itself.
 */
public final class EnchantmentLevelColors {
	private static final TextColor RED = TextColor.fromRgb(0xFF0000);
	private static final TextColor ORANGE = TextColor.fromRgb(0xFFA500);
	private static final TextColor SKY_BLUE = TextColor.fromRgb(0x87CEEB);

	private EnchantmentLevelColors() {
	}

	/**
	 * Returns the color for the given level, or null if it should keep the
	 * enchantment's normal color (levels 1-5, and anything past 20).
	 */
	public static TextColor colorFor(int level) {
		if (level >= 6 && level <= 10) {
			return RED;
		}
		if (level >= 11 && level <= 15) {
			return ORANGE;
		}
		if (level >= 16 && level <= 20) {
			return SKY_BLUE;
		}
		return null;
	}
}
