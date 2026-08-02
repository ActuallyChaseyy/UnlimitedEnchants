package com.femboy.unlimitedenchants.mixin;

import com.femboy.unlimitedenchants.internal.LootGenerationContext;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mob.populateDefaultEquipmentEnchantments() (e.g. a raid captain's banner-marked
 * weapon) calls straight into this method, so marking loot generation active for
 * its duration covers that path the same way LootTableSuppressionMixin covers loot
 * tables - neither goes through the other, so both are needed.
 */
@Mixin(EnchantmentHelper.class)
public class EnchantmentProviderSuppressionMixin {
	@Inject(method = "enchantItemFromProvider", at = @At("HEAD"))
	private static void unlimitedEnchants$enter(ItemStack stack, RegistryAccess registryAccess,
			ResourceKey<EnchantmentProvider> providerKey, DifficultyInstance difficulty, RandomSource random,
			CallbackInfo ci) {
		LootGenerationContext.enter();
	}

	@Inject(method = "enchantItemFromProvider", at = @At("RETURN"))
	private static void unlimitedEnchants$exit(ItemStack stack, RegistryAccess registryAccess,
			ResourceKey<EnchantmentProvider> providerKey, DifficultyInstance difficulty, RandomSource random,
			CallbackInfo ci) {
		LootGenerationContext.exit();
	}
}
