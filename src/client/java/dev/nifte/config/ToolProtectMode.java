package dev.nifte.config;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum ToolProtectMode {
	DISABLED("Disabled"),
	ENCHANTED_ONLY("Enchanted only"),
	ALL_TOOLS("All tools");

	private final String label;

	ToolProtectMode(String label) {
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
