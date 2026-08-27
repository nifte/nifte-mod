package dev.nifte.feature.fullbright;

import dev.nifte.config.NifteConfig;
import dev.nifte.hud.ToggleOverlay;

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

		ToggleOverlay.show("nifte.fullbright.toggle", config.fullbrightEnabled);
	}
}
