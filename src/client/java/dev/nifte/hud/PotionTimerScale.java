package dev.nifte.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

final class PotionTimerScale {
	private static final float LONG_SCALE = 0.8F;
	private static final int MAX_WIDTH = PotionEffectIcon.SIZE - 2;

	private PotionTimerScale() {
	}

	static float of(Font font, Component duration) {
		if (digits(duration.getString()) <= 3) {
			return 1.0F;
		}

		int width = font.width(duration);
		float fitted = (float) MAX_WIDTH / (float) width;
		return Math.min(LONG_SCALE, fitted);
	}

	private static int digits(String text) {
		int count = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) >= '0' && text.charAt(i) <= '9') {
				count++;
			}
		}

		return count;
	}
}
