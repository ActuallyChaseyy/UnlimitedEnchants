package com.femboy.unlimitedenchants.mixin;

import com.femboy.unlimitedenchants.internal.EnchantmentLevelColors;
import com.femboy.unlimitedenchants.internal.RomanNumerals;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Enchantment.getFullname() renders the level suffix via Component.translatable
 * ("enchantment.level." + level), but vanilla's lang file only defines that key for
 * levels 1-10 (as roman numerals I-X); anything higher falls back to showing the raw,
 * untranslated key. Redirecting that lookup to compute the roman numeral directly
 * keeps the display consistent (and locale-independent) at any level.
 * Levels above vanilla's normal 1-5 range also get the whole name recolored via
 * EnchantmentLevelColors, overriding the usual gray/curse-red entirely.
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
				return Component.literal(RomanNumerals.toRoman(level));
			} catch (NumberFormatException ignored) {
				// Not actually a level key somehow - fall through to vanilla behavior.
			}
		}
		return Component.translatable(key);
	}

	@Inject(method = "getFullname", at = @At("RETURN"), cancellable = true)
	private static void unlimitedEnchants$colorWholeName(Holder<Enchantment> enchantment, int level,
			CallbackInfoReturnable<Component> cir) {
		TextColor color = EnchantmentLevelColors.colorFor(level);
		if (color != null && cir.getReturnValue() instanceof MutableComponent mutable) {
			cir.setReturnValue(mutable.withStyle(Style.EMPTY.withColor(color)));
		}
	}
}
