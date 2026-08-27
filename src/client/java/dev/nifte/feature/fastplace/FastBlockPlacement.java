package dev.nifte.feature.fastplace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;

public final class FastBlockPlacement {
	private static @Nullable Direction lockedFace;

	private FastBlockPlacement() {
	}

	public static void beforeTick(Minecraft minecraft) {
		if (!enabled() || minecraft.player == null || minecraft.options == null || !minecraft.options.keyUse.isDown()) {
			lockedFace = null;
		}
	}

	public static boolean shouldSkipDelay(LocalPlayer player) {
		return lockedFace != null && holdingBlock(player);
	}

	public static void onUseOn(LocalPlayer player, InteractionHand hand, BlockHitResult hit) {
		if (lockedFace != null || !enabled() || !isBlockItem(player, hand) || !isClickedFace(hit)) {
			return;
		}

		lockedFace = hit.getDirection();
	}

	public static boolean shouldBlockPlacement(LocalPlayer player, InteractionHand hand, BlockHitResult hit) {
		return lockedFace != null && isBlockItem(player, hand) && !sameFace(hit);
	}

	private static boolean holdingBlock(LocalPlayer player) {
		if (!enabled() || player == null || player.isSpectator()) {
			return false;
		}

		for (InteractionHand hand : InteractionHand.values()) {
			if (isBlockItem(player, hand)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isBlockItem(LocalPlayer player, InteractionHand hand) {
		return player.getItemInHand(hand).getItem() instanceof BlockItem;
	}

	private static boolean sameFace(BlockHitResult hit) {
		return isClickedFace(hit) && hit.getDirection() == lockedFace;
	}

	private static boolean isClickedFace(BlockHitResult hit) {
		return hit.getType() == HitResult.Type.BLOCK && !hit.isInside();
	}

	private static boolean enabled() {
		return NifteConfig.get().fastBlockPlacementEnabled;
	}
}
