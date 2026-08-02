package com.femboy.unlimitedenchants.mixin;

import com.femboy.unlimitedenchants.internal.EnchantmentLoreBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ItemStack.set(DataComponentType, T) is the one place every higher-level API that
 * changes an item's enchantments - the enchanting table, anvil combining, /enchant,
 * loot functions, ItemStack.enchant() - ultimately bottoms out, since that's the
 * only way component data actually gets persisted onto a stack. Watching it here
 * for the enchantments/stored_enchantments component types means the lore gets
 * refreshed no matter which path changed them.
 */
@Mixin(ItemStack.class)
public abstract class EnchantmentLoreMixin {
	@Inject(method = "set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
			at = @At("RETURN"))
	private <T> void unlimitedEnchants$refreshEnchantLore(DataComponentType<T> type, T value,
			CallbackInfoReturnable<T> cir) {
		if (type == DataComponents.ENCHANTMENTS || type == DataComponents.STORED_ENCHANTMENTS) {
			@SuppressWarnings("unchecked")
			DataComponentType<ItemEnchantments> enchantmentsType = (DataComponentType<ItemEnchantments>) type;
			EnchantmentLoreBuilder.refresh((ItemStack) (Object) this, enchantmentsType);
		}
	}
}
