package dev.nifte.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

final class ProjectileTrajectoryPath {
	private static final int MAX_TICKS = 300;
	private static final float ENTITY_HIT_MARGIN = 0.3F;

	private ProjectileTrajectoryPath() {
	}

	static List<Vec3> trace(Level level, LocalPlayer player, Flight flight) {
		List<Vec3> points = new ArrayList<>();
		Vec3 position = flight.origin();
		Vec3 velocity = flight.velocity();
		points.add(position);

		for (int tick = 0; tick < MAX_TICKS; tick++) {
			if (!level.hasChunk(SectionPos.blockToSectionCoord(position.x), SectionPos.blockToSectionCoord(position.z))) {
				break;
			}

			boolean water = inWater(level, position);
			Vec3 movement = stepStart(flight, velocity, water);
			velocity = stepVelocity(flight, velocity, water);

			Vec3 next = position.add(movement);
			HitResult hit = clip(level, player, position, next);
			if (hit.getType() != HitResult.Type.MISS) {
				points.add(hit.getLocation());
				break;
			}

			position = next;
			points.add(position);
			if (position.y < level.getMinY() - 64) {
				break;
			}
		}

		return points;
	}

	static List<Vec3> fromHand(List<Vec3> physics, Vec3 visualStart) {
		int size = physics.size();
		if (size == 0) {
			return physics;
		}

		Vec3 offset = visualStart.subtract(physics.getFirst());
		if (offset.lengthSqr() < 1.0E-8) {
			return physics;
		}

		double[] along = new double[size];
		for (int i = 1; i < size; i++) {
			along[i] = along[i - 1] + physics.get(i).distanceTo(physics.get(i - 1));
		}

		double length = along[size - 1];
		if (length < 1.0E-4) {
			List<Vec3> points = new ArrayList<>(size);
			points.add(visualStart);
			for (int i = 1; i < size; i++) {
				points.add(physics.get(i));
			}

			return points;
		}

		List<Vec3> points = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			double t = along[i] / length;
			double fade = t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
			points.add(physics.get(i).add(offset.scale(1.0 - fade)));
		}

		return points;
	}

	private static Vec3 stepStart(Flight flight, Vec3 velocity, boolean water) {
		return switch (flight.step()) {
			case THROWABLE -> applyDrag(applyGravity(velocity, flight.gravity()), water ? flight.waterDrag() : flight.airDrag());
			case WIND -> applyDrag(velocity, water ? flight.waterDrag() : flight.airDrag());
			case ARROW -> velocity;
		};
	}

	private static Vec3 stepVelocity(Flight flight, Vec3 velocity, boolean water) {
		return switch (flight.step()) {
			case THROWABLE, WIND -> stepStart(flight, velocity, water);
			case ARROW -> {
				Vec3 next = water ? applyDrag(velocity, flight.waterDrag()) : applyDrag(velocity, flight.airDrag());
				yield applyGravity(next, flight.gravity());
			}
		};
	}

	private static Vec3 applyGravity(Vec3 velocity, double gravity) {
		return gravity == 0.0 ? velocity : velocity.add(0.0, -gravity, 0.0);
	}

	private static Vec3 applyDrag(Vec3 velocity, float drag) {
		return drag == 1.0F ? velocity : velocity.scale(drag);
	}

	private static boolean inWater(Level level, Vec3 position) {
		return level.getFluidState(BlockPos.containing(position)).is(FluidTags.WATER);
	}

	private static HitResult clip(Level level, LocalPlayer player, Vec3 from, Vec3 to) {
		BlockHitResult blockHit = level.clipIncludingBorder(
			new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())
		);
		Vec3 clipTo = blockHit.getType() == HitResult.Type.MISS ? to : blockHit.getLocation();
		AABB search = new AABB(from, clipTo).inflate(1.0);
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
			level,
			player,
			from,
			clipTo,
			search,
			Entity::canBeHitByProjectile,
			ENTITY_HIT_MARGIN
		);
		return entityHit == null ? blockHit : entityHit;
	}

	record Flight(Vec3 origin, Vec3 velocity, double gravity, float airDrag, float waterDrag, Step step) {
		static Flight throwable(Vec3 origin, Vec3 velocity, double gravity) {
			return new Flight(origin, velocity, gravity, 0.99F, 0.8F, Step.THROWABLE);
		}

		static Flight arrow(Vec3 origin, Vec3 velocity, float waterDrag) {
			return new Flight(origin, velocity, 0.05, 0.99F, waterDrag, Step.ARROW);
		}

		static Flight wind(Vec3 origin, Vec3 velocity) {
			return new Flight(origin, velocity, 0.0, 1.0F, 1.0F, Step.WIND);
		}
	}

	enum Step {
		THROWABLE,
		ARROW,
		WIND
	}
}
