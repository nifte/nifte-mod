package dev.nifte.hud;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ToggleOverlay {
	private ToggleOverlay() {
	}

	public static void show(String messageKey, boolean enabled) {
		Component status = Component.translatable(
			enabled ? "nifte.config.enabled" : "nifte.config.disabled"
		).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
		Minecraft.getInstance().gui.hud.setOverlayMessage(Component.translatable(messageKey, status), false);
	}
}
