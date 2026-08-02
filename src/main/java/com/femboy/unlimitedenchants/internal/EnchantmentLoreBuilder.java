package com.femboy.unlimitedenchants.internal;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;

/**
 * Bakes each enchantment's fully-resolved name (colored roman numeral included)
 * directly into the item's lore, and hides the vanilla enchantment tooltip section
 * so it doesn't duplicate underneath.
 * This mod runs server-side only. The normal enchantment tooltip is built by
 * client-side code calling Enchantment.getFullname() on whatever Enchantment class
 * the CLIENT has loaded - for a vanilla client, that's vanilla's own unpatched
 * version, which doesn't know about our roman-numeral/color mixins at all. Baking
 * the already-resolved Component into lore instead means the client only ever has
 * to display static data the server computed and sent over, no client-side logic
 * (or the mod itself) required.
 * ItemLore's single-arg constructor merges in a default italic, dark-purple style
 * for whatever a line doesn't already set - we use the two-arg constructor with our
 * own lines as the "styled" copy instead, so nothing but our own colors ever apply.
 */
public final class EnchantmentLoreBuilder {
	private EnchantmentLoreBuilder() {
	}

	public static void refresh(ItemStack stack, DataComponentType<ItemEnchantments> componentType) {
		ItemEnchantments enchantments = stack.getOrDefault(componentType, ItemEnchantments.EMPTY);
		TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);

		if (enchantments.isEmpty()) {
			stack.set(DataComponents.LORE, ItemLore.EMPTY);
			stack.set(DataComponents.TOOLTIP_DISPLAY, display.withHidden(componentType, false));
			return;
		}

		List<Component> lines = enchantments.entrySet().stream()
				.<Component>map(entry -> Enchantment.getFullname(entry.getKey(), entry.getIntValue()))
				.toList();

		stack.set(DataComponents.LORE, new ItemLore(lines, lines));
		stack.set(DataComponents.TOOLTIP_DISPLAY, display.withHidden(componentType, true));
	}
}
