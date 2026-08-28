package dev.nifte.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;

import dev.nifte.config.NifteConfig;

public final class CompassHud {
	private CompassHud() {
	}

	public static int bossBarOffset() {
		return visible() ? CompassHudLayout.bossBarOffset(NifteConfig.get().compassScale) : 0;
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (!visible()) {
			return;
		}

		NifteConfig config = NifteConfig.get();
		Minecraft minecraft = Minecraft.getInstance();
		float heading = CompassHudLayout.heading(lookYaw(minecraft));
		int width = Math.round(CompassHudLayout.WIDTH * config.compassScale);
		int x = (graphics.guiWidth() - width) / 2;
		int y = CompassHudLayout.TOP;

		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(config.compassScale, config.compassScale);
		draw(graphics, minecraft.font, heading);
		graphics.pose().popMatrix();
	}

	private static boolean visible() {
		NifteConfig config = NifteConfig.get();
		Minecraft minecraft = Minecraft.getInstance();
		return config.compassHudEnabled && minecraft.player != null;
	}

	private static float lookYaw(Minecraft minecraft) {
		float yaw = minecraft.gameRenderer.mainCamera().yRot();
		if (minecraft.options.getCameraType().isMirrored()) {
			yaw += 180.0F;
		}

		return yaw;
	}

	private static void draw(GuiGraphicsExtractor graphics, Font font, float heading) {
		for (int degree = 0; degree < 360; degree += CompassHudLayout.TICK_STEP) {
			if (!CompassHudLayout.visible(heading, degree)) {
				continue;
			}

			int x = CompassHudLayout.x(heading, degree);
			float alpha = CompassHudLayout.edgeAlpha(heading, degree);
			int tickHeight = CompassHudLayout.tickHeight(degree);
			graphics.fill(
				x,
				CompassHudLayout.TICK_BOTTOM - tickHeight,
				x + 1,
				CompassHudLayout.TICK_BOTTOM,
				ARGB.color(alpha, CompassHudLayout.rgb(degree))
			);

			String label = CompassHudLayout.label(degree);
			if (label != null) {
				drawLabel(graphics, font, label, x, ARGB.color(alpha, CompassHudLayout.rgb(degree)));
			}
		}
	}

	private static void drawLabel(GuiGraphicsExtractor graphics, Font font, String label, int x, int color) {
		float scale = CompassHudLayout.TEXT_SCALE;
		graphics.pose().pushMatrix();
		graphics.pose().translate(CompassHudLayout.labelX(x, font.width(label)), CompassHudLayout.LABEL_Y);
		graphics.pose().scale(scale, scale);
		graphics.text(font, label, 0, 0, color, true);
		graphics.pose().popMatrix();
	}
}
