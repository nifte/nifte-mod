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
	static final int ARMOR_ROW = 10;

	private MobHealthHudLayout() {
	}

	static boolean previewOnRight(HudAnchor anchor) {
		return anchor == HudAnchor.TOP_RIGHT || anchor == HudAnchor.BOTTOM_RIGHT;
	}

	static boolean hasArmor(int armor) {
		return armor > 0;
	}

	static int heartsWidth(int containers) {
		if (containers <= 0) {
			return 0;
		}

		return (Math.min(containers, HEARTS_PER_ROW) - 1) * HEART_STEP + HEART_SIZE;
	}

	static int armorWidth() {
		return (MobHealthArmor.ICONS - 1) * HEART_STEP + HEART_SIZE;
	}

	static int barsWidth(int containers, int armor) {
		return hasArmor(armor) ? Math.max(heartsWidth(containers), armorWidth()) : heartsWidth(containers);
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

	static int barsHeight(int containers, int armor) {
		return heartsHeight(containers) + (hasArmor(armor) ? ARMOR_ROW : 0);
	}

	static int width(int containers, int armor) {
		return PREVIEW_WIDTH + GAP + barsWidth(containers, armor);
	}

	static int height(int containers, int armor) {
		return Math.max(PREVIEW_HEIGHT, barsHeight(containers, armor));
	}

	static int previewX(HudAnchor anchor, int containers, int armor) {
		return previewOnRight(anchor) ? barsWidth(containers, armor) + GAP : 0;
	}

	static int previewY(int containers, int armor) {
		return (height(containers, armor) - PREVIEW_HEIGHT) / 2;
	}

	static int barsX(HudAnchor anchor, int containers, int armor) {
		return previewOnRight(anchor) ? 0 : PREVIEW_WIDTH + GAP;
	}

	static int barsY(int containers, int armor) {
		return (height(containers, armor) - barsHeight(containers, armor)) / 2;
	}

	static int armorY(int containers, int armor) {
		return barsY(containers, armor);
	}

	static int heartsY(int containers, int armor) {
		return barsY(containers, armor) + (hasArmor(armor) ? ARMOR_ROW : 0);
	}
}
