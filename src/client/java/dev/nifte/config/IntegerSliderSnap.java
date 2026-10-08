package dev.nifte.config;

import net.minecraft.util.Mth;

public final class IntegerSliderSnap {
	private IntegerSliderSnap() {
	}

	public static int snap(int min, int max, double progress) {
		if (max <= min) {
			return min;
		}

		int snapped = min + (int) Math.round(Mth.clamp(progress, 0.0, 1.0) * (max - min));
		return Mth.clamp(snapped, min, max);
	}

	public static double progress(int min, int max, int value) {
		if (max <= min) {
			return 0.0;
		}

		return (Mth.clamp(value, min, max) - min) / (double) (max - min);
	}
}
