package dev.nifte.feature.zoom;

import net.minecraft.util.Mth;

import dev.nifte.config.NifteConfig;
import dev.nifte.config.ZoomTransition;
import dev.nifte.input.NifteKeybinds;

public final class ZoomFeature {
	private static final float SMOOTH_FACTOR = 0.5F;
	private static final float SETTLE_EPSILON = 0.05F;

	private static boolean wasZooming;
	private static float sessionFov = 30.0F;
	private static float lastVanillaFov = Float.NaN;
	private static float oldAnimatedFov = Float.NaN;
	private static float animatedFov = Float.NaN;

	private ZoomFeature() {
	}

	public static void tick() {
		NifteConfig config = NifteConfig.get();
		if (!config.zoomEnabled || !NifteKeybinds.isBound(NifteKeybinds.zoom)) {
			wasZooming = false;
			sessionFov = config.zoomFov;
			resetAnimation();
			return;
		}

		boolean zooming = isZooming();
		if (zooming && !wasZooming) {
			sessionFov = config.zoomFov;
		} else if (!zooming) {
			sessionFov = config.zoomFov;
		}

		wasZooming = zooming;
		advanceAnimation(zooming ? sessionFov : lastVanillaFov, transition());
	}

	public static boolean isZooming() {
		return NifteConfig.get().zoomEnabled
			&& NifteKeybinds.isBound(NifteKeybinds.zoom)
			&& NifteKeybinds.zoom.isDown();
	}

	public static float modifyFov(float fov, float partialTicks) {
		lastVanillaFov = fov;
		if (Float.isNaN(animatedFov)) {
			oldAnimatedFov = fov;
			animatedFov = fov;
		}

		if (!transition().isSmooth()) {
			return isZooming() ? sessionFov : fov;
		}

		if (!isZooming() && settled(fov)) {
			return fov;
		}

		return Mth.lerp(partialTicks, oldAnimatedFov, animatedFov);
	}

	public static boolean handleScroll(double scrollY) {
		if (!isZooming() || scrollY == 0.0) {
			return false;
		}

		sessionFov = Mth.clamp(sessionFov - (float) Math.signum(scrollY) * 5.0F, 10.0F, 70.0F);
		return true;
	}

	public static double sensitivityMultiplier() {
		if (!isAffectingLook()) {
			return 1.0;
		}

		float fov = displayedFov();
		return (Float.isNaN(fov) ? sessionFov : fov) / 70.0;
	}

	private static boolean isAffectingLook() {
		if (isZooming()) {
			return true;
		}

		return transition().isSmooth()
			&& Float.isFinite(animatedFov)
			&& Float.isFinite(lastVanillaFov)
			&& !settled(lastVanillaFov);
	}

	private static float displayedFov() {
		if (!transition().isSmooth()) {
			return sessionFov;
		}

		return animatedFov;
	}

	private static ZoomTransition transition() {
		ZoomTransition transition = NifteConfig.get().zoomTransition;
		return transition == null ? ZoomTransition.SMOOTH : transition;
	}

	private static void advanceAnimation(float target, ZoomTransition transition) {
		if (Float.isNaN(target) || Float.isNaN(animatedFov)) {
			return;
		}

		if (!transition.isSmooth()) {
			oldAnimatedFov = target;
			animatedFov = target;
			return;
		}

		oldAnimatedFov = animatedFov;
		animatedFov += (target - animatedFov) * SMOOTH_FACTOR;
		if (Math.abs(animatedFov - target) < SETTLE_EPSILON) {
			animatedFov = target;
		}
	}

	private static boolean settled(float target) {
		return Float.isFinite(animatedFov)
			&& Float.isFinite(oldAnimatedFov)
			&& Math.abs(animatedFov - target) < SETTLE_EPSILON
			&& Math.abs(oldAnimatedFov - target) < SETTLE_EPSILON;
	}

	private static void resetAnimation() {
		lastVanillaFov = Float.NaN;
		oldAnimatedFov = Float.NaN;
		animatedFov = Float.NaN;
	}
}
