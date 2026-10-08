package dev.nifte.feature.progress;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;

public final class ProgressBar {
	private static final float ALMOST_DONE = 0.999F;

	private ProgressBar() {
	}

	public static float attackStrength(LocalPlayer player, float adjustTicks) {
		NifteConfig config = NifteConfig.get();
		if (!config.progressBarEnabled) {
			return player.getAttackStrengthScale(adjustTicks);
		}

		float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		Float progress = active(player, config, partialTick);
		if (progress == null) {
			return player.getAttackStrengthScale(adjustTicks);
		}

		return Mth.clamp(progress, 0.0F, ALMOST_DONE);
	}

	private static @Nullable Float active(LocalPlayer player, NifteConfig config, float partialTick) {
		Float mining = MiningProgress.of(player, config.progressBarMining, partialTick);
		if (mining != null) {
			return mining;
		}

		Float eating = EatingProgress.of(player, config.progressBarEating, partialTick);
		if (eating != null) {
			return eating;
		}

		Float drawing = DrawingProgress.of(player, config.progressBarDrawing, partialTick);
		if (drawing != null) {
			return drawing;
		}

		return CooldownProgress.of(player, config.progressBarCooldowns, partialTick);
	}
}
