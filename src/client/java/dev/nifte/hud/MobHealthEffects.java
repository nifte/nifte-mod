package dev.nifte.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import org.jspecify.annotations.Nullable;

final class MobHealthEffects {
	private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("hud/effect_background");
	private static final Identifier BACKGROUND_AMBIENT = Identifier.withDefaultNamespace("hud/effect_background_ambient");
	private static final int TEXT_COLOR = 0xFFFFFFFF;

	private MobHealthEffects() {
	}

	static List<MobEffectInstance> visible(LivingEntity entity) {
		List<MobEffectInstance> effects = new ArrayList<>();
		for (MobEffectInstance effect : entity.getActiveEffects()) {
			if (effect.showIcon()) {
				effects.add(effect);
			}
		}

		effects.sort(Comparator.reverseOrder());
		return effects;
	}

	static void draw(GuiGraphicsExtractor graphics, Font font, List<MobEffectInstance> effects, int x, int y, int width) {
		int perRow = MobHealthHudLayout.iconsPerRow(width);
		int step = MobHealthHudLayout.EFFECT_FRAME + MobHealthHudLayout.EFFECT_GAP;
		for (int index = 0; index < effects.size(); index++) {
			MobEffectInstance effect = effects.get(index);
			int column = index % perRow;
			int row = index / perRow;
			icon(graphics, font, effect, x + column * step, y + row * step);
		}
	}

	private static void icon(GuiGraphicsExtractor graphics, Font font, MobEffectInstance effect, int x, int y) {
		graphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			effect.isAmbient() ? BACKGROUND_AMBIENT : BACKGROUND,
			x,
			y,
			MobHealthHudLayout.EFFECT_FRAME,
			MobHealthHudLayout.EFFECT_FRAME
		);
		graphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			Hud.getMobEffectSprite(effect.getEffect()),
			x + MobHealthHudLayout.EFFECT_ICON_INSET,
			y + MobHealthHudLayout.EFFECT_ICON_INSET,
			MobHealthHudLayout.EFFECT_ICON,
			MobHealthHudLayout.EFFECT_ICON
		);
		drawLevel(graphics, font, level(effect), x, y);
	}

	private static void drawLevel(GuiGraphicsExtractor graphics, Font font, @Nullable Component level, int x, int y) {
		if (level == null) {
			return;
		}

		int textWidth = font.width(level);
		float scale = Math.min(1.0F, (MobHealthHudLayout.EFFECT_FRAME - 2) / (float) Math.max(textWidth, 1));
		int drawWidth = Math.round(textWidth * scale);
		int textX = x + (MobHealthHudLayout.EFFECT_FRAME - drawWidth) / 2;
		int textY = y + MobHealthHudLayout.EFFECT_FRAME - Math.round(font.lineHeight * scale);
		graphics.pose().pushMatrix();
		graphics.pose().translate(textX, textY);
		graphics.pose().scale(scale, scale);
		graphics.text(font, level, 0, 0, TEXT_COLOR, true);
		graphics.pose().popMatrix();
	}

	private static @Nullable Component level(MobEffectInstance effect) {
		int amplifier = effect.getAmplifier();
		if (amplifier < 1) {
			return null;
		}

		if (amplifier <= 9) {
			return Component.translatable("enchantment.level." + (amplifier + 1));
		}

		return Component.literal(Integer.toString(amplifier + 1));
	}
}
