package dev.nifte.hud;

import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

final class PlayerTracerGeometry {
	private static final double MIN_DEPTH = 0.25;

	private PlayerTracerGeometry() {
	}

	static boolean visible(LocalPlayer self, Player player, double rangeSq) {
		return !(player instanceof LocalPlayer)
			&& player.isAlive()
			&& !player.isSpectator()
			&& self.distanceToSqr(player) <= rangeSq;
	}

	static Vec3 cursor(Camera camera) {
		Vec3 near = camera.getNearPlane(camera.getFov()).getPointOnPlane(0.0F, 0.0F);
		double length = near.length();
		if (length < 1.0E-4) {
			return camera.position().add(new Vec3(camera.forwardVector()).scale(MIN_DEPTH));
		}

		return camera.position().add(near.scale(MIN_DEPTH / length));
	}

	static Vec3 target(Camera camera, Player player, float partialTick) {
		return inFrontOfCamera(camera, player.getPosition(partialTick).add(0.0, player.getBbHeight() * 0.5, 0.0));
	}

	private static Vec3 inFrontOfCamera(Camera camera, Vec3 point) {
		Vec3 origin = camera.position();
		Vec3 forward = new Vec3(camera.forwardVector());
		Vec3 toPoint = point.subtract(origin);
		double along = toPoint.dot(forward);
		if (along >= MIN_DEPTH) {
			return point;
		}

		return origin.add(forward.scale(MIN_DEPTH)).add(toPoint.subtract(forward.scale(along)));
	}
}
