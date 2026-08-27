package dev.nifte.feature.overlay;

import dev.nifte.config.NifteConfig;

public final class FireOverlayFeature {
	public static final float MIN_OFFSET = 0.0F;
	public static final float MAX_OFFSET = 0.6F;
	public static final float DEFAULT_OFFSET = 0.35F;

	private FireOverlayFeature() {
	}

	public static float yOffset() {
		NifteConfig config = NifteConfig.get();
		if (!config.lowerFireOverlay) {
			return 0.0F;
		}

		return config.fireOverlayOffset;
	}
}
