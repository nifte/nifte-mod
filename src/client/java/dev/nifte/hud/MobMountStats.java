package dev.nifte.hud;

import java.util.Locale;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Llama;

import org.jspecify.annotations.Nullable;

final class MobMountStats {
	static final int LINES = 2;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final double GROUND_DRAG = 0.6 * 0.91;
	private static final double INPUT_SCALE = 0.98;
	private static final double TICKS_PER_SECOND = 20.0;
	private static final double VERTICAL_DRAG = 0.98;
	private static final int MAX_JUMP_STEPS = 4096;

	private final Component speed;
	private final Component jump;

	private MobMountStats(Component speed, Component jump) {
		this.speed = speed;
		this.jump = jump;
	}

	Component speed() {
		return this.speed;
	}

	Component jump() {
		return this.jump;
	}

	static @Nullable MobMountStats of(LivingEntity entity) {
		if (!(entity instanceof AbstractHorse horse) || horse instanceof Llama) {
			return null;
		}

		return new MobMountStats(
			Component.translatable("nifte.hud.mount.speed", format(blocksPerSecond(horse))),
			Component.translatable("nifte.hud.mount.jump", format(jumpHeight(horse)))
		);
	}

	static void draw(GuiGraphicsExtractor graphics, Font font, MobMountStats stats, int x, int y) {
		graphics.text(font, stats.speed, x, y, TEXT_COLOR, true);
		graphics.text(font, stats.jump, x, y + font.lineHeight, TEXT_COLOR, true);
	}

	private static String format(double value) {
		return String.format(Locale.ROOT, "%.2f", value);
	}

	// Distance traveled each second on a normal block. Forward input is scaled by 0.98,
	// then the horse moves by its velocity before ground drag (block friction 0.6 times air drag 0.91) is applied.
	private static double blocksPerSecond(AbstractHorse horse) {
		double movementSpeed = horse.getAttributeValue(Attributes.MOVEMENT_SPEED);
		return TICKS_PER_SECOND * movementSpeed * INPUT_SCALE / (1.0 - GROUND_DRAG);
	}

	// A full jump moves by the current vertical velocity, then subtracts gravity and scales by vertical drag.
	private static double jumpHeight(AbstractHorse horse) {
		double velocity = horse.getAttributeValue(Attributes.JUMP_STRENGTH) + horse.getJumpBoostPower();
		double gravity = horse.getAttributeValue(Attributes.GRAVITY);
		double drag = verticalDrag(horse.getAttributeValue(Attributes.AIR_DRAG_MODIFIER));
		double height = 0.0;
		for (int step = 0; step < MAX_JUMP_STEPS && velocity > 0.0; step++) {
			height += velocity;
			velocity = (velocity - gravity) * drag;
		}

		return height;
	}

	private static double verticalDrag(double airDragModifier) {
		return Mth.clamp(1.0 - (1.0 - VERTICAL_DRAG) * airDragModifier, 0.0, 1.0);
	}
}
