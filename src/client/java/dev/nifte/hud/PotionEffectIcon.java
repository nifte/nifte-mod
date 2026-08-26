package dev.nifte.hud;

import net.minecraft.world.effect.MobEffectInstance;

public record PotionEffectIcon(MobEffectInstance effect, int x, int y) {
	public static final int SIZE = 24;

	public int centeredTextX(int textWidth, float scale) {
		return this.x + Math.round((SIZE - textWidth * scale) / 2.0F);
	}

	public int textY(int lineHeight, float scale) {
		return this.y + SIZE - Math.round(lineHeight * scale) - 1;
	}
}
