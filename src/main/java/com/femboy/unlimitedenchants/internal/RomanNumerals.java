package com.femboy.unlimitedenchants.internal;

public final class RomanNumerals {
	private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
	private static final String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

	private RomanNumerals() {
	}

	public static String toRoman(int number) {
		if (number <= 0) {
			// Roman numerals have no representation for zero or negatives; these
			// shouldn't occur for an enchantment level, but fall back plainly if they do.
			return Integer.toString(number);
		}

		StringBuilder result = new StringBuilder();
		int remaining = number;
		for (int i = 0; i < VALUES.length; i++) {
			while (remaining >= VALUES[i]) {
				remaining -= VALUES[i];
				result.append(SYMBOLS[i]);
			}
		}
		return result.toString();
	}
}
