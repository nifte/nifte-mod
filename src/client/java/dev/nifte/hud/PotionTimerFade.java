package dev.nifte.hud;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

final class PotionTimerFade {
	private static final int BLINK_TICKS = 200;
	private static final float MIN_ALPHA = 0.12F;
	private static final float PULSE_TICKS = 4.0F;

	private PotionTimerFade() {
	}

	static float alpha(MobEffectInstance effect) {
		if (!effect.endsWithin(BLINK_TICKS)) {
			return 1.0F;
		}

		float pulse = (1.0F + Mth.cos(effect.getDuration() * (float) Math.PI / PULSE_TICKS)) * 0.5F;
		return MIN_ALPHA + pulse * (1.0F - MIN_ALPHA);
	}
}
