package dev.nifte.feature.progress;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;

import org.jspecify.annotations.Nullable;

final class DrawingProgress {
	private static final float BOW_DRAW_TICKS = 20.0F;

	private DrawingProgress() {
	}

	static @Nullable Float of(LocalPlayer player, boolean enabled, float partialTick) {
		if (!enabled || !player.isUsingItem()) {
			return null;
		}

		ItemStack stack = player.getUseItem();
		float used = player.getTicksUsingItem(partialTick);
		ItemUseAnimation animation = stack.getUseAnimation();
		if (animation == ItemUseAnimation.BOW) {
			return bowPower(used);
		}

		if (animation == ItemUseAnimation.CROSSBOW) {
			return crossbowPower(stack, player, used);
		}

		return null;
	}

	private static float bowPower(float ticks) {
		float pull = ticks / BOW_DRAW_TICKS;
		return (pull * pull + pull * 2.0F) / 3.0F;
	}

	private static @Nullable Float crossbowPower(ItemStack stack, LocalPlayer player, float used) {
		int duration = stack.getItem() instanceof CrossbowItem
			? CrossbowItem.getChargeDuration(stack, player)
			: stack.getUseDuration(player);
		if (duration <= 0) {
			return null;
		}

		return used / duration;
	}
}
