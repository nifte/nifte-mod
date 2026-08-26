package dev.nifte.hud;

import net.minecraft.world.item.ItemStack;

final class ArmorHudPiece {
	private final ItemStack stack;
	private final String text;
	private final int color;
	private final int textWidth;

	ArmorHudPiece(ItemStack stack, String text, int color, int textWidth) {
		this.stack = stack;
		this.text = text;
		this.color = color;
		this.textWidth = textWidth;
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
}
