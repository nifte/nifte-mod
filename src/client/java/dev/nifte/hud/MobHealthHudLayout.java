package dev.nifte.hud;

import net.minecraft.util.Mth;

import dev.nifte.config.HudAnchor;

final class MobHealthHudLayout {
	static final int PREVIEW_WIDTH = 48;
	static final int PREVIEW_HEIGHT = 56;
	static final int GAP = 4;
	static final int HEART_SIZE = 9;
	static final int HEART_STEP = 8;
	static final int HEARTS_PER_ROW = 10;

	private MobHealthHudLayout() {
	}

	static boolean previewOnRight(HudAnchor anchor) {
		return anchor == HudAnchor.TOP_RIGHT || anchor == HudAnchor.BOTTOM_RIGHT;
	}

	static int heartsWidth(int containers) {
		if (containers <= 0) {
			return 0;
		}

		return (Math.min(containers, HEARTS_PER_ROW) - 1) * HEART_STEP + HEART_SIZE;
	}

	static int rowCount(int containers) {
		return Math.max(1, Mth.ceil(containers / (float) HEARTS_PER_ROW));
	}

	static int rowHeight(int rows) {
		return Math.max(10 - (rows - 2), 3);
	}

	static int heartsHeight(int containers) {
		int rows = rowCount(containers);
		return (rows - 1) * rowHeight(rows) + HEART_SIZE;
	}

	static int width(int containers) {
		return PREVIEW_WIDTH + GAP + heartsWidth(containers);
	}

	static int height(int containers) {
		return Math.max(PREVIEW_HEIGHT, heartsHeight(containers));
	}

	static int previewX(HudAnchor anchor, int containers) {
		return previewOnRight(anchor) ? heartsWidth(containers) + GAP : 0;
	}

	static int previewY(int containers) {
		return (height(containers) - PREVIEW_HEIGHT) / 2;
	}

	static int heartsX(HudAnchor anchor, int containers) {
		return previewOnRight(anchor) ? 0 : PREVIEW_WIDTH + GAP;
	}

	static int heartsY(int containers) {
		return (height(containers) - heartsHeight(containers)) / 2;
	}
}
