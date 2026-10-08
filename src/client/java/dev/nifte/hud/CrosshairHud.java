package dev.nifte.hud;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import dev.nifte.config.NifteConfig;

public final class CrosshairHud {
	private static final Identifier SPRITE = Identifier.withDefaultNamespace("hud/crosshair");

	private CrosshairHud() {
	}

	public static void blit(
		GuiGraphicsExtractor graphics,
		RenderPipeline pipeline,
		Identifier sprite,
		int x,
		int y,
		int width,
		int height
	) {
		float factor = SPRITE.equals(sprite) ? factor() : 1.0F;
		if (factor == 1.0F) {
			graphics.blitSprite(pipeline, sprite, x, y, width, height);
			return;
		}

		graphics.blitSprite(
			pipeline,
			sprite,
			scaledCoord(x, graphics.guiWidth(), factor),
			scaledCoord(y, graphics.guiHeight(), factor),
			scaledSize(width, factor),
			scaledSize(height, factor)
		);
	}

	private static float factor() {
		int configured = Mth.clamp(
			NifteConfig.get().crosshairScale,
			NifteConfig.CROSSHAIR_SCALE_MATCH_GUI,
			NifteConfig.CROSSHAIR_SCALE_MAX
		);
		if (configured == NifteConfig.CROSSHAIR_SCALE_MATCH_GUI) {
			return 1.0F;
		}

		int guiScale = Math.max(1, Minecraft.getInstance().getWindow().getGuiScale());
		return configured / (float) guiScale;
	}

	private static int scaledCoord(int coord, int guiSize, float factor) {
		float center = guiSize / 2.0F;
		return Math.round(center + (coord - center) * factor);
	}

	private static int scaledSize(int size, float factor) {
		if (size <= 0) {
			return size;
		}

		return Math.max(1, Math.round(size * factor));
	}
}
