package dev.nifte.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import dev.nifte.config.NifteConfig;

public final class PotionHud {
	private static final int WHITE = 0xFFFFFF;

	private PotionHud() {
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		NifteConfig config = NifteConfig.get();
		Minecraft minecraft = Minecraft.getInstance();
		if (!config.potionHudEnabled || minecraft.player == null || minecraft.level == null) {
			return;
		}

		float tickrate = minecraft.level.tickRateManager().tickrate();
		for (PotionEffectIcon icon : PotionEffectHudLayout.vanillaIcons(minecraft, graphics.guiWidth())) {
			if (icon.effect().isAmbient()) {
				continue;
			}

			Component duration = PotionTimerFormat.format(icon.effect(), tickrate);
			float scale = PotionTimerScale.of(minecraft.font, duration);
			int textWidth = minecraft.font.width(duration);
			int color = ARGB.color(PotionTimerFade.alpha(icon.effect()), WHITE);
			graphics.pose().pushMatrix();
			graphics.pose().translate(icon.centeredTextX(textWidth, scale), icon.textY(minecraft.font.lineHeight, scale));
			graphics.pose().scale(scale, scale);
			graphics.text(minecraft.font, duration, 0, 0, color, true);
			graphics.pose().popMatrix();
		}
	}
}
