package dev.nifte.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

final class HudLineOcclusion {
	private static final double HIT_SLACK = 1.0E-3;
	private static final int TRANSITION_STEPS = 8;

	private HudLineOcclusion() {
	}

	static List<AABB> occluderBoxes(Level level, Entity viewer, Vec3 camera, List<Vec3> points, float partialTick) {
		AABB search = bounds(camera, points).inflate(1.0);
		List<AABB> boxes = new ArrayList<>();
		for (Entity entity : level.getEntities(viewer, search, entity -> occludes(viewer, entity))) {
			boxes.add(interpolatedBox(entity, partialTick));
		}

		return boxes;
	}

	static List<List<Vec3>> visibleSpans(Level level, Vec3 camera, List<Vec3> points, List<AABB> occluderBoxes) {
		List<List<Vec3>> spans = new ArrayList<>();
		if (points.size() < 2) {
			return spans;
		}

		List<Vec3> current = new ArrayList<>();
		boolean previousVisible = visible(level, camera, points.getFirst(), occluderBoxes);
		if (previousVisible) {
			current.add(points.getFirst());
		}

		for (int i = 1; i < points.size(); i++) {
			Vec3 point = points.get(i);
			boolean isVisible = visible(level, camera, point, occluderBoxes);
			if (isVisible) {
				if (!previousVisible) {
					current.add(edge(level, camera, points.get(i - 1), point, occluderBoxes));
				}

				current.add(point);
			} else if (previousVisible) {
				current.add(edge(level, camera, points.get(i - 1), point, occluderBoxes));
				if (current.size() >= 2) {
					spans.add(current);
				}

				current = new ArrayList<>();
			}

			previousVisible = isVisible;
		}

		if (current.size() >= 2) {
			spans.add(current);
		}

		return spans;
	}

	static boolean visible(Level level, Vec3 camera, Vec3 point, List<AABB> occluderBoxes) {
		if (point.distanceToSqr(camera) <= HIT_SLACK) {
			return true;
		}

		if (blockedByEntity(camera, point, occluderBoxes)) {
			return false;
		}

		BlockHitResult hit = level.clip(
			new ClipContext(camera, point, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty())
		);
		return hit.getType() == HitResult.Type.MISS || camera.distanceToSqr(hit.getLocation()) >= camera.distanceToSqr(point) - HIT_SLACK;
	}

	private static boolean blockedByEntity(Vec3 camera, Vec3 point, List<AABB> occluderBoxes) {
		double pointDist = camera.distanceToSqr(point);
		for (AABB box : occluderBoxes) {
			if (box.contains(point)) {
				return true;
			}

			Optional<Vec3> hit = box.clip(camera, point);
			if (hit.isPresent() && camera.distanceToSqr(hit.get()) < pointDist - HIT_SLACK) {
				return true;
			}
		}

		return false;
	}

	private static Vec3 edge(Level level, Vec3 camera, Vec3 from, Vec3 to, List<AABB> occluderBoxes) {
		boolean fromVisible = visible(level, camera, from, occluderBoxes);
		Vec3 visible = fromVisible ? from : to;
		Vec3 hidden = fromVisible ? to : from;
		for (int step = 0; step < TRANSITION_STEPS; step++) {
			Vec3 mid = visible.add(hidden).scale(0.5);
			if (visible(level, camera, mid, occluderBoxes)) {
				visible = mid;
			} else {
				hidden = mid;
			}
		}

		return visible;
	}

	private static boolean occludes(Entity viewer, Entity entity) {
		if (entity == viewer || !entity.isAlive() || entity.isSpectator()) {
			return false;
		}

		return entity instanceof LivingEntity || entity instanceof EnderDragonPart;
	}

	private static AABB interpolatedBox(Entity entity, float partialTick) {
		AABB box = entity.getBoundingBox();
		Vec3 offset = entity.getPosition(partialTick).subtract(entity.position());
		return offset.lengthSqr() < HIT_SLACK ? box : box.move(offset);
	}

	private static AABB bounds(Vec3 camera, List<Vec3> points) {
		double minX = camera.x;
		double minY = camera.y;
		double minZ = camera.z;
		double maxX = minX;
		double maxY = minY;
		double maxZ = minZ;
		for (Vec3 point : points) {
			minX = Math.min(minX, point.x);
			minY = Math.min(minY, point.y);
			minZ = Math.min(minZ, point.z);
			maxX = Math.max(maxX, point.x);
			maxY = Math.max(maxY, point.y);
			maxZ = Math.max(maxZ, point.z);
		}

		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
