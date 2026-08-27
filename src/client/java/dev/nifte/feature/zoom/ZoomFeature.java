package dev.nifte.feature.zoom;

import net.minecraft.util.Mth;

import dev.nifte.config.NifteConfig;
import dev.nifte.input.NifteKeybinds;

public final class ZoomFeature {
	private static boolean wasZooming;
	private static float sessionFov = 30.0F;

	private ZoomFeature() {
	}

	public static void tick() {
		NifteConfig config = NifteConfig.get();
		if (!config.zoomEnabled || !NifteKeybinds.isBound(NifteKeybinds.zoom)) {
			wasZooming = false;
			sessionFov = config.zoomFov;
			return;
		}

		boolean zooming = isZooming();
		if (zooming && !wasZooming) {
			sessionFov = config.zoomFov;
		} else if (!zooming) {
			sessionFov = config.zoomFov;
		}

		wasZooming = zooming;
	}

	public static boolean isZooming() {
		return NifteConfig.get().zoomEnabled
			&& NifteKeybinds.isBound(NifteKeybinds.zoom)
			&& NifteKeybinds.zoom.isDown();
	}

	public static float modifyFov(float fov) {
		if (!isZooming()) {
			return fov;
		}

		return sessionFov;
	}

	public static boolean handleScroll(double scrollY) {
		if (!isZooming() || scrollY == 0.0) {
			return false;
		}

		sessionFov = Mth.clamp(sessionFov - (float) Math.signum(scrollY) * 5.0F, 10.0F, 70.0F);
		return true;
	}

	public static double sensitivityMultiplier() {
		if (!isZooming()) {
			return 1.0;
		}

		return sessionFov / 70.0;
	}
}
