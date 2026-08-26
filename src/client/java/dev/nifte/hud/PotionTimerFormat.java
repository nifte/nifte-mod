package dev.nifte.hud;

import java.util.Locale;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

final class PotionTimerFormat {
	private PotionTimerFormat() {
	}

	static Component format(MobEffectInstance effect, float tickrate) {
		if (effect.isInfiniteDuration()) {
			return Component.translatable("effect.duration.infinite");
		}

		int seconds = Mth.floor(effect.getDuration() / tickrate);
		int minutes = seconds / 60;
		seconds %= 60;
		int hours = minutes / 60;
		minutes %= 60;
		if (hours > 0) {
			return Component.literal(String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds));
		}

		if (minutes > 0) {
			return Component.literal(String.format(Locale.ROOT, "%d:%02d", minutes, seconds));
		}

		return Component.literal(Integer.toString(seconds));
	}
}
