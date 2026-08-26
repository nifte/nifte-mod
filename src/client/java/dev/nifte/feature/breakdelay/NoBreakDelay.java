package dev.nifte.feature.breakdelay;

import dev.nifte.config.NifteConfig;

public final class NoBreakDelay {
	private NoBreakDelay() {
	}

	public static boolean enabled() {
		return NifteConfig.get().noBreakDelayEnabled;
	}
}
