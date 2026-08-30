package dev.nifte.hud;

import java.util.List;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;

final class HudLineGeometry {
	private static final double MIN_DEPTH = 0.05;
	private static final double MIN_LENGTH_SQ = 1.0E-10;

	private HudLineGeometry() {
	}

	record Rails(Vec3[] left, Vec3[] right) {
	}

	static @Nullable Rails rails(Camera camera, List<Vec3> points, float pixelWidth, int screenHeight) {
		int count = points.size();
		if (count < 2 || pixelWidth <= 0.0F || screenHeight <= 0) {
			return null;
		}

		double worldPerPixelAtUnitDepth = 2.0 * Math.tan(Math.toRadians(camera.getFov()) * 0.5) / screenHeight;
		Vec3[] left = new Vec3[count];
		Vec3[] right = new Vec3[count];
		Vec3 previousSide = null;
		for (int i = 0; i < count; i++) {
			Vec3 point = points.get(i);
			Vec3 side = side(camera, point, tangent(points, i), previousSide);
			double halfWidth = pixelWidth * 0.5 * worldPerPixelAtUnitDepth * depth(camera, point);
			Vec3 offset = side.scale(halfWidth);
			left[i] = point.add(offset);
			right[i] = point.subtract(offset);
			previousSide = side;
		}

		return new Rails(left, right);
	}

	private static Vec3 tangent(List<Vec3> points, int index) {
		if (index == 0) {
			return fallbackTangent(points.get(1).subtract(points.getFirst()));
		}

		if (index == points.size() - 1) {
			return fallbackTangent(points.get(index).subtract(points.get(index - 1)));
		}

		Vec3 incoming = points.get(index).subtract(points.get(index - 1));
		Vec3 outgoing = points.get(index + 1).subtract(points.get(index));
		double incomingLengthSq = incoming.lengthSqr();
		double outgoingLengthSq = outgoing.lengthSqr();
		if (incomingLengthSq < MIN_LENGTH_SQ) {
			return fallbackTangent(outgoing);
		}

		if (outgoingLengthSq < MIN_LENGTH_SQ) {
			return fallbackTangent(incoming);
		}

		return incoming.scale(1.0 / Math.sqrt(incomingLengthSq)).add(outgoing.scale(1.0 / Math.sqrt(outgoingLengthSq)));
	}

	private static Vec3 side(Camera camera, Vec3 point, Vec3 tangent, @Nullable Vec3 previousSide) {
		Vec3 side = tangent.cross(camera.position().subtract(point));
		if (side.lengthSqr() < MIN_LENGTH_SQ) {
			side = new Vec3(camera.leftVector());
		}

		if (side.lengthSqr() < MIN_LENGTH_SQ) {
			side = new Vec3(0.0, 1.0, 0.0);
		}

		if (previousSide != null && side.dot(previousSide) < 0.0) {
			side = side.scale(-1.0);
		}

		return side.normalize();
	}

	private static double depth(Camera camera, Vec3 point) {
		double along = point.subtract(camera.position()).dot(new Vec3(camera.forwardVector()));
		return Math.max(along, MIN_DEPTH);
	}

	private static Vec3 fallbackTangent(Vec3 tangent) {
		return tangent.lengthSqr() < MIN_LENGTH_SQ ? new Vec3(0.0, 0.0, 1.0) : tangent;
	}
}
