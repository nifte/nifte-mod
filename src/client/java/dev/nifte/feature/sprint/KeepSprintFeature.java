package dev.nifte.feature.sprint;

import dev.nifte.config.NifteConfig;

public final class KeepSprintFeature {
	private KeepSprintFeature() {
	}

	public static boolean enabled() {
		return NifteConfig.get().keepSprintOnWallEnabled;
	}
}
