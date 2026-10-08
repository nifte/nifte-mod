package dev.nifte.feature.progress;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

final class CooldownProgress {
	private CooldownProgress() {
	}

	static @Nullable Float of(LocalPlayer player, boolean enabled, float partialTick) {
		if (!enabled) {
			return null;
		}

		float remaining = 0.0F;
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
			remaining = Math.max(remaining, remaining(player, inventory.getItem(slot), partialTick));
		}

		remaining = Math.max(remaining, remaining(player, player.getOffhandItem(), partialTick));
		if (remaining <= 0.0F) {
			return null;
		}

		return 1.0F - remaining;
	}

	private static float remaining(LocalPlayer player, ItemStack stack, float partialTick) {
		if (stack.isEmpty()) {
			return 0.0F;
		}

		return player.getCooldowns().getCooldownPercent(stack, partialTick);
	}
}
