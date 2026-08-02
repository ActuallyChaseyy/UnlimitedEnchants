package com.femboy.unlimitedenchants.mixin;

import com.femboy.unlimitedenchants.internal.LootGenerationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Every public overload of LootTable.getRandomItems(...) ultimately delegates to
 * this one method, so marking loot generation as active here (and clearing it once
 * done) covers every loot table roll - chests, mob drops, fishing, block loot,
 * everything - regardless of which overload the caller used.
 */
@Mixin(LootTable.class)
public class LootTableSuppressionMixin {
	@Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
			at = @At("HEAD"))
	private void unlimitedEnchants$enter(LootContext context, Consumer<ItemStack> consumer, CallbackInfo ci) {
		LootGenerationContext.enter();
	}

	@Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
			at = @At("RETURN"))
	private void unlimitedEnchants$exit(LootContext context, Consumer<ItemStack> consumer, CallbackInfo ci) {
		LootGenerationContext.exit();
	}
}
