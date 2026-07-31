package com.femboy.unlimitedenchants.mixin;

import com.femboy.unlimitedenchants.internal.EnchantmentLevelColors;
import com.femboy.unlimitedenchants.internal.RomanNumerals;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Enchantment.getFullname() renders the level suffix via Component.translatable
 * ("enchantment.level." + level), but vanilla's lang file only defines that key for
 * levels 1-10 (as roman numerals I-X); anything higher falls back to showing the raw,
 * untranslated key. Redirecting that lookup to compute the roman numeral directly
 * keeps the display consistent (and locale-independent) at any level, and lets levels
 * above vanilla's normal 1-5 range get their own color band via EnchantmentLevelColors.
 */
@Mixin(Enchantment.class)
public class EnchantmentRomanNumeralMixin {
	private static final String LEVEL_KEY_PREFIX = "enchantment.level.";

	@Redirect(method = "getFullname", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
	private static MutableComponent unlimitedEnchants$romanLevel(String key) {
		if (key.startsWith(LEVEL_KEY_PREFIX)) {
			try {
				int level = Integer.parseInt(key.substring(LEVEL_KEY_PREFIX.length()));
				MutableComponent roman = Component.literal(RomanNumerals.toRoman(level));
				TextColor color = EnchantmentLevelColors.colorFor(level);
				return color == null ? roman : roman.withStyle(Style.EMPTY.withColor(color));
			} catch (NumberFormatException ignored) {
				// Not actually a level key somehow - fall through to vanilla behavior.
			}
		}
		return Component.translatable(key);
	}
}
