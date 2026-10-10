package dev.nifte.hud;

import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

final class ArmorHudPiece {
	private final ItemStack stack;
	private final String text;
	private final int color;
	private final int textWidth;
	private final @Nullable String iconCount;

	ArmorHudPiece(ItemStack stack, String text, int color, int textWidth) {
		this(stack, text, color, textWidth, null);
	}

	private ArmorHudPiece(ItemStack stack, String text, int color, int textWidth, @Nullable String iconCount) {
		this.stack = stack;
		this.text = text;
		this.color = color;
		this.textWidth = textWidth;
		this.iconCount = iconCount;
	}

	ArmorHudPiece withIconCount(String iconCount) {
		return new ArmorHudPiece(this.stack, this.text, this.color, this.textWidth, iconCount);
	}

	ItemStack stack() {
		return this.stack;
	}

	String text() {
		return this.text;
	}

	int color() {
		return this.color;
	}

	int textWidth() {
		return this.textWidth;
	}

	@Nullable String iconCount() {
		return this.iconCount;
	}
}
