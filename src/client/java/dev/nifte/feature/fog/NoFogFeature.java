package dev.nifte.feature.fog;

import net.minecraft.client.renderer.fog.FogData;

import dev.nifte.config.NifteConfig;

public final class NoFogFeature {
	private NoFogFeature() {
	}

	public static void apply(FogData fog) {
		if (!NifteConfig.get().disableFog) {
			return;
		}

		fog.environmentalStart = Float.MAX_VALUE;
		fog.environmentalEnd = Float.MAX_VALUE;
		fog.renderDistanceStart = Float.MAX_VALUE;
		fog.renderDistanceEnd = Float.MAX_VALUE;
		fog.skyEnd = Float.MAX_VALUE;
		fog.cloudEnd = Float.MAX_VALUE;
	}
}
