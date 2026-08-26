package dev.nifte.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import dev.nifte.config.ArmorHudAnchor;
import dev.nifte.config.HudAnchor;

public final class HudLayout {
	private HudLayout() {
	}

	public static int x(HudAnchor anchor, int guiWidth, int offsetX, int contentWidth) {
		return switch (anchor) {
			case TOP_LEFT, BOTTOM_LEFT -> offsetX;
			case TOP_RIGHT, BOTTOM_RIGHT -> guiWidth - offsetX - contentWidth;
		};
	}

	public static int x(ArmorHudAnchor anchor, int guiWidth, int offsetX, int contentWidth) {
		return switch (anchor) {
			case TOP_LEFT, BOTTOM_LEFT, HOTBAR -> offsetX;
			case TOP_RIGHT, BOTTOM_RIGHT -> guiWidth - offsetX - contentWidth;
		};
	}

	public static int y(HudAnchor anchor, int guiHeight, int offsetY, int contentHeight) {
		return switch (anchor) {
			case TOP_LEFT, TOP_RIGHT -> offsetY;
			case BOTTOM_LEFT, BOTTOM_RIGHT -> guiHeight - offsetY - contentHeight;
		};
	}

	public static int y(ArmorHudAnchor anchor, int guiHeight, int offsetY, int contentHeight) {
		return switch (anchor) {
			case TOP_LEFT, TOP_RIGHT, HOTBAR -> offsetY;
			case BOTTOM_LEFT, BOTTOM_RIGHT -> guiHeight - offsetY - contentHeight;
		};
	}

	public static void scaledText(
		GuiGraphicsExtractor graphics,
		Font font,
		String text,
		int x,
		int y,
		int rgb,
		float scale
	) {
		int color = 0xFF000000 | (rgb & 0xFFFFFF);
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(scale, scale);
		graphics.text(font, text, 0, 0, color, true);
		graphics.pose().popMatrix();
	}
}
