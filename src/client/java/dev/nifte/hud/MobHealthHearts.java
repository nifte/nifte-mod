package dev.nifte.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

final class MobHealthHearts {
	private static final Identifier CONTAINER = Identifier.withDefaultNamespace("hud/heart/container");
	private static final Identifier CONTAINER_HARDCORE = Identifier.withDefaultNamespace("hud/heart/container_hardcore");

	private MobHealthHearts() {
	}

	static int containers(LivingEntity entity) {
		int current = Mth.ceil(entity.getHealth());
		float max = Math.max(entity.getMaxHealth(), current);
		int absorption = Mth.ceil(entity.getAbsorptionAmount());
		return Math.max(1, Mth.ceil(max / 2.0F) + Mth.ceil(absorption / 2.0F));
	}

	static void draw(GuiGraphicsExtractor graphics, LivingEntity entity, int x, int y) {
		int current = Mth.ceil(entity.getHealth());
		float max = Math.max(entity.getMaxHealth(), current);
		int absorption = Mth.ceil(entity.getAbsorptionAmount());
		int healthContainers = Math.max(1, Mth.ceil(max / 2.0F));
		int absorptionContainers = Mth.ceil(absorption / 2.0F);
		int total = healthContainers + absorptionContainers;
		int rows = MobHealthHudLayout.rowCount(total);
		int rowHeight = MobHealthHudLayout.rowHeight(rows);
		boolean hardcore = entity instanceof Player && entity.level().getLevelData().isHardcore();
		HeartFill fill = HeartFill.of(entity);
		int healthHalves = healthContainers * 2;

		for (int index = total - 1; index >= 0; index--) {
			int row = index / MobHealthHudLayout.HEARTS_PER_ROW;
			int column = index % MobHealthHudLayout.HEARTS_PER_ROW;
			int heartX = x + column * MobHealthHudLayout.HEART_STEP;
			int heartY = y + row * rowHeight;
			heart(graphics, hardcore ? CONTAINER_HARDCORE : CONTAINER, heartX, heartY);

			int halves = index * 2;
			if (index >= healthContainers) {
				int absorptionHalves = halves - healthHalves;
				if (absorptionHalves < absorption) {
					heart(graphics, HeartFill.ABSORBING.sprite(absorptionHalves + 1 == absorption, hardcore), heartX, heartY);
				}
			} else if (halves < current) {
				heart(graphics, fill.sprite(halves + 1 == current, hardcore), heartX, heartY);
			}
		}
	}

	private static void heart(GuiGraphicsExtractor graphics, Identifier sprite, int x, int y) {
		graphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			sprite,
			x,
			y,
			MobHealthHudLayout.HEART_SIZE,
			MobHealthHudLayout.HEART_SIZE
		);
	}

	private enum HeartFill {
		NORMAL("full", "half", "hardcore_full", "hardcore_half"),
		POISONED("poisoned_full", "poisoned_half", "poisoned_hardcore_full", "poisoned_hardcore_half"),
		WITHERED("withered_full", "withered_half", "withered_hardcore_full", "withered_hardcore_half"),
		FROZEN("frozen_full", "frozen_half", "frozen_hardcore_full", "frozen_hardcore_half"),
		ABSORBING("absorbing_full", "absorbing_half", "absorbing_hardcore_full", "absorbing_hardcore_half");

		private final Identifier full;
		private final Identifier half;
		private final Identifier hardcoreFull;
		private final Identifier hardcoreHalf;

		HeartFill(String full, String half, String hardcoreFull, String hardcoreHalf) {
			this.full = heart(full);
			this.half = heart(half);
			this.hardcoreFull = heart(hardcoreFull);
			this.hardcoreHalf = heart(hardcoreHalf);
		}

		Identifier sprite(boolean halfHeart, boolean hardcore) {
			if (hardcore) {
				return halfHeart ? this.hardcoreHalf : this.hardcoreFull;
			}

			return halfHeart ? this.half : this.full;
		}

		static HeartFill of(LivingEntity entity) {
			if (entity.hasEffect(MobEffects.POISON)) {
				return POISONED;
			}

			if (entity.hasEffect(MobEffects.WITHER)) {
				return WITHERED;
			}

			if (entity.isFullyFrozen()) {
				return FROZEN;
			}

			return NORMAL;
		}

		private static Identifier heart(String path) {
			return Identifier.withDefaultNamespace("hud/heart/" + path);
		}
	}
}
