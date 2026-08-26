package dev.nifte.feature.fastplace;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;

import dev.nifte.config.NifteConfig;

public final class FastBlockPlacement {
	private FastBlockPlacement() {
	}

	public static boolean shouldSkipDelay(LocalPlayer player) {
		if (!NifteConfig.get().fastBlockPlacementEnabled || player == null || player.isSpectator()) {
			return false;
		}

		for (InteractionHand hand : InteractionHand.values()) {
			if (player.getItemInHand(hand).getItem() instanceof BlockItem) {
				return true;
			}
		}

		return false;
	}
}
