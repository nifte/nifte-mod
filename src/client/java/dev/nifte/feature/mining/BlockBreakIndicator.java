package dev.nifte.feature.mining;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;
import dev.nifte.mixin.MultiPlayerGameModeAccessor;

public final class BlockBreakIndicator {
	private static final float ALMOST_BROKEN = 0.999F;

	private BlockBreakIndicator() {
	}

	public static float attackStrength(LocalPlayer player, float adjustTicks) {
		if (!NifteConfig.get().blockBreakIndicatorEnabled) {
			return player.getAttackStrengthScale(adjustTicks);
		}

		Float mining = miningProgress(player);
		if (mining == null) {
			return player.getAttackStrengthScale(adjustTicks);
		}

		return mining;
	}

	private static @Nullable Float miningProgress(LocalPlayer player) {
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
		float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
		return Mth.clamp(gameMode.nifte$destroyProgress() + step * partialTick, 0.0F, ALMOST_BROKEN);
	}
}
