package dev.nifte.feature.zoom;

import net.minecraft.util.Mth;

import dev.nifte.config.NifteConfig;
import dev.nifte.config.ZoomTransition;
import dev.nifte.input.NifteKeybinds;

/**
 * Only takes control of the camera FOV and the look sensitivity while the zoom key is held, or while the smooth
 * transition back to the vanilla FOV is still running. Outside of that window the animation state is cleared so that
 * vanilla's own FOV changes (sprinting, flying, speed effects, bows, water) never alter the look sensitivity.
 */
public final class ZoomFeature {
	private static final float SMOOTH_FACTOR = 0.5F;
	private static final float SETTLE_EPSILON = 0.05F;
	private static final float MIN_FOV = 10.0F;
	private static final float MAX_FOV = 70.0F;
	private static final float SCROLL_STEP = 5.0F;

	private static float sessionFov = 30.0F;
	private static float lastVanillaFov = Float.NaN;
	private static float oldAnimatedFov = Float.NaN;
	private static float animatedFov = Float.NaN;

	private ZoomFeature() {
	}

	public static void tick() {
		NifteConfig config = NifteConfig.get();
		boolean zooming = isZooming();
		if (!zooming) {
			sessionFov = config.zoomFov;
		}

		if (!isAvailable(config) || !transition().isSmooth()) {
			clearAnimation();
			return;
		}

		if (zooming) {
			seedAnimationIfNeeded();
			advanceAnimation(sessionFov);
			return;
		}

		if (!isAnimating()) {
			return;
		}

		advanceAnimation(lastVanillaFov);
		if (settled(lastVanillaFov)) {
			clearAnimation();
		}
	}

	public static boolean isZooming() {
		return isAvailable(NifteConfig.get()) && NifteKeybinds.zoom.isDown();
	}

	public static float modifyFov(float fov, float partialTicks) {
		lastVanillaFov = fov;
		if (!transition().isSmooth()) {
			return isZooming() ? sessionFov : fov;
		}

		if (isZooming()) {
			seedAnimationIfNeeded();
		}

		if (!isAnimating()) {
			return fov;
		}

		return Mth.lerp(partialTicks, oldAnimatedFov, animatedFov);
	}

	public static boolean handleScroll(double scrollY) {
		if (!isZooming() || scrollY == 0.0) {
			return false;
		}

		sessionFov = Mth.clamp(sessionFov - (float) Math.signum(scrollY) * SCROLL_STEP, MIN_FOV, MAX_FOV);
		return true;
	}

	public static double sensitivityMultiplier() {
		if (!hasVanillaFov()) {
			return 1.0;
		}

		if (!transition().isSmooth()) {
			return isZooming() ? sessionFov / lastVanillaFov : 1.0;
		}

		if (!isAnimating()) {
			return 1.0;
		}

		return animatedFov / lastVanillaFov;
	}

	private static boolean isAvailable(NifteConfig config) {
		return config.zoomEnabled && NifteKeybinds.isBound(NifteKeybinds.zoom);
	}

	private static ZoomTransition transition() {
		ZoomTransition transition = NifteConfig.get().zoomTransition;
		return transition == null ? ZoomTransition.SMOOTH : transition;
	}

	private static boolean hasVanillaFov() {
		return Float.isFinite(lastVanillaFov) && lastVanillaFov > 0.0F;
	}

	private static boolean isAnimating() {
		return Float.isFinite(animatedFov);
	}

	private static void seedAnimationIfNeeded() {
		if (isAnimating() || !hasVanillaFov()) {
			return;
		}

		oldAnimatedFov = lastVanillaFov;
		animatedFov = lastVanillaFov;
	}

	private static void advanceAnimation(float target) {
		if (!isAnimating() || !Float.isFinite(target)) {
			return;
		}

		oldAnimatedFov = animatedFov;
		animatedFov += (target - animatedFov) * SMOOTH_FACTOR;
		if (Math.abs(animatedFov - target) < SETTLE_EPSILON) {
			animatedFov = target;
		}
	}

	private static boolean settled(float target) {
		return isAnimating()
			&& Float.isFinite(target)
			&& Math.abs(animatedFov - target) < SETTLE_EPSILON
			&& Math.abs(oldAnimatedFov - target) < SETTLE_EPSILON;
	}

	private static void clearAnimation() {
		oldAnimatedFov = Float.NaN;
		animatedFov = Float.NaN;
	}
}
