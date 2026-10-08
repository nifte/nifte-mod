package dev.nifte.feature.progress;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

import dev.nifte.mixin.MultiPlayerGameModeAccessor;

final class MiningProgress {
	private MiningProgress() {
	}

	static @Nullable Float of(LocalPlayer player, boolean enabled, float partialTick) {
		if (!enabled) {
			return null;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (!(minecraft.gameMode instanceof MultiPlayerGameModeAccessor gameMode) || !gameMode.nifte$isDestroying()) {
			return null;
		}

		ClientLevel level = minecraft.level;
		if (level == null) {
			return null;
		}

		BlockPos pos = gameMode.nifte$destroyBlockPos();
		BlockState state = level.getBlockState(pos);
		float step = state.getDestroyProgress(player, level, pos);
		return gameMode.nifte$destroyProgress() + step * partialTick;
	}
}
