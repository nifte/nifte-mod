package dev.nifte.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

final class MobHealthArmor {
	private static final Identifier EMPTY = Identifier.withDefaultNamespace("hud/armor_empty");
	private static final Identifier HALF = Identifier.withDefaultNamespace("hud/armor_half");
	private static final Identifier FULL = Identifier.withDefaultNamespace("hud/armor_full");
	static final int ICONS = 10;

	private MobHealthArmor() {
	}

	static int of(LivingEntity entity) {
		return Math.max(entity.getArmorValue(), 0);
	}

	static void draw(GuiGraphicsExtractor graphics, int armor, int x, int y) {
		if (armor <= 0) {
			return;
		}

		for (int i = 0; i < ICONS; i++) {
			int point = i * 2 + 1;
			Identifier sprite = point < armor ? FULL : point == armor ? HALF : EMPTY;
			graphics.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				sprite,
				x + i * MobHealthHudLayout.HEART_STEP,
				y,
				MobHealthHudLayout.HEART_SIZE,
				MobHealthHudLayout.HEART_SIZE
			);
		}
	}
}
