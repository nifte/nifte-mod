package dev.nifte.config;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum DropConfirmMode {
	DISABLED("Disabled"),
	TOOLS_AND_WEAPONS("Tools and weapons"),
	ENCHANTED("Enchanted only"),
	ALL("All items");

	private final String label;

	DropConfirmMode(String label) {
		this.label = label;
	}

	public boolean isEnabled() {
		return this != DISABLED;
	}

	public Component optionLabel() {
		return Component.literal(this.label).withStyle(this.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED);
	}

	@Override
	public String toString() {
		return this.label;
	}
}
