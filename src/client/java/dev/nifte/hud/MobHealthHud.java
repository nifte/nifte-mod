package dev.nifte.hud;

import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;
import dev.nifte.config.NifteConfigPreview;

public final class MobHealthHud {
	private static @Nullable LivingEntity remembered;
	private static long lastLookMillis;

	private MobHealthHud() {
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		NifteConfig config = NifteConfig.get();
		Minecraft minecraft = Minecraft.getInstance();
		if (!config.mobHealthEnabled || minecraft.player == null || minecraft.gui.hud.isHidden()) {
			return;
		}

		if (minecraft.gui.screen() != null && !NifteConfigPreview.isOpen()) {
			return;
		}

		float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
		LivingEntity target = overlay(minecraft, config, partialTick);
		if (target == null) {
			return;
		}

		int containers = MobHealthHearts.containers(target);
		int armor = MobHealthArmor.of(target);
		Font font = minecraft.font;
		List<MobEffectInstance> effects = MobHealthEffects.visible(target);
		MobMountStats stats = MobMountStats.of(target);
		int statsWidth = stats == null ? 0 : Math.max(font.width(stats.speed()), font.width(stats.jump()));
		int statLines = stats == null ? 0 : MobMountStats.LINES;
		int width = MobHealthHudLayout.width(containers, armor, statsWidth);
		int effectRows = MobHealthHudLayout.effectRows(effects.size(), width);
		int height = MobHealthHudLayout.height(containers, armor, effectRows, statLines, font.lineHeight);
		int x = HudLayout.x(config.mobHealthAnchor, graphics.guiWidth(), config.mobHealthOffsetX, Math.round(width * config.mobHealthScale));
		int y = HudLayout.y(config.mobHealthAnchor, graphics.guiHeight(), config.mobHealthOffsetY, Math.round(height * config.mobHealthScale));
		int previewX = x + Math.round(MobHealthHudLayout.previewX(config.mobHealthAnchor, containers, armor) * config.mobHealthScale);
		int previewY = y + Math.round(MobHealthHudLayout.previewY(containers, armor) * config.mobHealthScale);
		int previewWidth = Math.round(MobHealthHudLayout.PREVIEW_WIDTH * config.mobHealthScale);
		int previewHeight = Math.round(MobHealthHudLayout.PREVIEW_HEIGHT * config.mobHealthScale);
		int barsX = MobHealthHudLayout.barsX(config.mobHealthAnchor, containers, armor);

		MobHealthPreview.extract(
			graphics,
			target,
			partialTick,
			previewX,
			previewY,
			previewX + previewWidth,
			previewY + previewHeight
		);

		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(config.mobHealthScale, config.mobHealthScale);
		MobHealthArmor.draw(graphics, armor, barsX, MobHealthHudLayout.armorY(containers, armor));
		MobHealthHearts.draw(graphics, target, barsX, MobHealthHudLayout.heartsY(containers, armor));
		if (!effects.isEmpty()) {
			MobHealthEffects.draw(graphics, font, effects, 0, MobHealthHudLayout.effectsY(containers, armor), width);
		}

		if (stats != null) {
			MobMountStats.draw(graphics, font, stats, 0, MobHealthHudLayout.statsY(containers, armor, effectRows));
		}

		graphics.pose().popMatrix();
	}

	private static @Nullable LivingEntity overlay(Minecraft minecraft, NifteConfig config, float partialTick) {
		LivingEntity looking = target(minecraft, partialTick, config.mobHealthReach);
		long now = Util.getMillis();
		if (looking != null) {
			remembered = looking;
			lastLookMillis = now;
			return looking;
		}

		if (remembered == null || config.mobHealthFadeSeconds <= 0 || !usable(minecraft, remembered)) {
			remembered = null;
			return null;
		}

		if (now - lastLookMillis >= config.mobHealthFadeSeconds * 1000L) {
			remembered = null;
			return null;
		}

		return remembered;
	}

	private static @Nullable LivingEntity target(Minecraft minecraft, float partialTick, float reach) {
		LivingEntity picked = MobHealthTarget.find(minecraft, partialTick, reach);
		if (picked == null || !usable(minecraft, picked)) {
			return null;
		}

		return picked;
	}

	private static boolean usable(Minecraft minecraft, LivingEntity entity) {
		return entity.isAlive()
			&& !entity.isRemoved()
			&& entity.level() == minecraft.level
			&& (entity.getMaxHealth() > 0.0F || entity.getHealth() > 0.0F);
	}
}
