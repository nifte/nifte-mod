package dev.nifte.feature.fullbright;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import dev.nifte.config.NifteConfig;

public final class FullbrightFeature {
	private FullbrightFeature() {
	}

	public static boolean isActive() {
		return NifteConfig.get().fullbrightEnabled;
	}

	public static void toggle() {
		NifteConfig config = NifteConfig.get();
		config.fullbrightEnabled = !config.fullbrightEnabled;
		NifteConfig.save();

		Component message = Component.translatable(
			config.fullbrightEnabled ? "nifte.fullbright.enabled" : "nifte.fullbright.disabled"
		);
		Minecraft.getInstance().gui.hud.setOverlayMessage(message, false);
	}
}
