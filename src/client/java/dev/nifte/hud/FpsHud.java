package dev.nifte.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import dev.nifte.config.NifteConfig;

public final class FpsHud {
	private FpsHud() {
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		NifteConfig config = NifteConfig.get();
		Minecraft minecraft = Minecraft.getInstance();
		if (!config.fpsEnabled || minecraft.player == null) {
			return;
		}

		if (config.fpsHideWithDebug && minecraft.getDebugOverlay().showDebugScreen()) {
			return;
		}

		String text = minecraft.getFps() + " FPS";
		int width = (int) (minecraft.font.width(text) * config.fpsScale);
		int height = (int) (minecraft.font.lineHeight * config.fpsScale);
		int x = HudLayout.x(config.fpsAnchor, graphics.guiWidth(), config.fpsOffsetX, width);
		int y = HudLayout.y(config.fpsAnchor, graphics.guiHeight(), config.fpsOffsetY, height);
		HudLayout.scaledText(graphics, minecraft.font, text, x, y, config.fpsColor, config.fpsScale);
	}
}
