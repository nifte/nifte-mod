package dev.nifte.feature.progress;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

final class EatingProgress {
	private EatingProgress() {
	}

	static @Nullable Float of(LocalPlayer player, boolean enabled, float partialTick) {
		if (!enabled || !player.isUsingItem()) {
			return null;
		}

		ItemStack stack = player.getUseItem();
		if (stack.get(DataComponents.FOOD) == null) {
			return null;
		}

		int duration = stack.getUseDuration(player);
		if (duration <= 0) {
			return null;
		}

		return player.getTicksUsingItem(partialTick) / duration;
	}
}
