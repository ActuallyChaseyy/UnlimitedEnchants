package com.femboy.unlimitedenchants.mixin;

import com.femboy.unlimitedenchants.config.ModConfig;
import com.femboy.unlimitedenchants.internal.BookshelfLevelCap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * EnchantmentCostMixin's cost plateau lets any enchantment whose vanilla max level
 * is cheap (Mending, Silk Touch, curses - anything capped at 1 in vanilla) satisfy
 * its own cost bound at almost any bookshelf power, jumping straight to the
 * configured max regardless of how few bookshelves are present. This clamps the
 * final result of a table roll to whatever BookshelfLevelCap says the current,
 * live bookshelf count actually allows - recomputed fresh here rather than cached
 * from slotsChanged, since that can run well before the player actually clicks.
 * maxTableEnchant is folded in as a second, independent ceiling on top of that -
 * BookshelfLevelCap's curve targets whichever of maxLevel/maxTableEnchant is lower,
 * so the table can be capped below maxLevel without touching anvil combining,
 * /enchant, or loot, which all still read maxLevel directly.
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentTableBookshelfCapMixin {
	@Shadow
	@Final
	private ContainerLevelAccess access;

	@Inject(method = "getEnchantmentList", at = @At("RETURN"), cancellable = true)
	private void unlimitedEnchants$capByBookshelves(RegistryAccess registryAccess, ItemStack itemStack, int slot,
			int cost, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
		List<EnchantmentInstance> original = cir.getReturnValue();
		if (original.isEmpty()) {
			return;
		}

		int bookshelves = this.access.evaluate(EnchantmentTableBookshelfCapMixin::countBookshelves, 0);
		int ceiling = Math.min(ModConfig.getMaxLevel(), ModConfig.getMaxTableEnchant());
		int cap = BookshelfLevelCap.capFor(bookshelves, ceiling);

		cir.setReturnValue(original.stream()
				.map(instance -> instance.level() > cap
						? new EnchantmentInstance(instance.enchantment(), cap)
						: instance)
				.toList());
	}

	private static int countBookshelves(Level level, BlockPos tablePos) {
		int count = 0;
		for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
			if (EnchantingTableBlock.isValidBookShelf(level, tablePos, offset)) {
				count++;
			}
		}
		return count;
	}
}
