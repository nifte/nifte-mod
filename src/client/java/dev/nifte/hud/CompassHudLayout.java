package dev.nifte.hud;

import net.minecraft.util.Mth;

final class CompassHudLayout {
	static final int WIDTH = 180;
	static final int HEIGHT = 10;
	static final int TOP = 2;
	static final int GAP = 4;
	static final int LABEL_Y = 1;
	static final float TEXT_SCALE = 0.7F;
	static final int TICK_BOTTOM = 10;
	static final int CARDINAL_TICK = 3;
	static final int INTERCARDINAL_TICK = 2;
	static final int MINOR_TICK = 1;
	static final int TICK_STEP = 15;
	static final float PIXELS_PER_DEGREE = 1.0F;
	static final int NORTH = 0xFF5555;
	static final int CARDINAL = 0xFFFFFF;
	static final int INTERCARDINAL = 0xC8C8C8;
	static final int MINOR = 0x8C8C8C;

	private CompassHudLayout() {
	}

	static float heading(float yaw) {
		return Mth.wrapDegrees(yaw + 180.0F);
	}

	static int x(float heading, int degree) {
		return Math.round(WIDTH / 2.0F + Mth.wrapDegrees(degree - heading) * PIXELS_PER_DEGREE);
	}

	static boolean visible(float heading, int degree) {
		float pixels = Math.abs(Mth.wrapDegrees(degree - heading)) * PIXELS_PER_DEGREE;
		return pixels <= WIDTH / 2.0F + 12.0F;
	}

	static float edgeAlpha(float heading, int degree) {
		float pixels = Math.abs(Mth.wrapDegrees(degree - heading)) * PIXELS_PER_DEGREE;
		float half = WIDTH / 2.0F;
		float fadeStart = half * 0.65F;
		if (pixels <= fadeStart) {
			return 1.0F;
		}

		return Mth.clamp(1.0F - (pixels - fadeStart) / (half - fadeStart), 0.0F, 1.0F);
	}

	static float labelX(int tickX, int textWidth) {
		return tickX + 0.5F - textWidth * TEXT_SCALE / 2.0F;
	}

	static int rgb(int degree) {
		if (degree == 0) {
			return NORTH;
		}

		if (degree % 90 == 0) {
			return CARDINAL;
		}

		if (degree % 45 == 0) {
			return INTERCARDINAL;
		}

		return MINOR;
	}

	static int tickHeight(int degree) {
		if (degree % 90 == 0) {
			return CARDINAL_TICK;
		}

		if (degree % 45 == 0) {
			return INTERCARDINAL_TICK;
		}

		return MINOR_TICK;
	}

	static String label(int degree) {
		return switch (degree) {
			case 0 -> "N";
			case 45 -> "NE";
			case 90 -> "E";
			case 135 -> "SE";
			case 180 -> "S";
			case 225 -> "SW";
			case 270 -> "W";
			case 315 -> "NW";
			default -> null;
		};
	}

	static int bossBarOffset(float scale) {
		return TOP + Math.round(HEIGHT * scale) + GAP;
	}
}
