package dev.nifte.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;

final class MobHealthTarget {
	private MobHealthTarget() {
	}

	static @Nullable LivingEntity find(Minecraft minecraft, float partialTick, float reach) {
		Entity camera = minecraft.getCameraEntity();
		if (camera == null || minecraft.level == null || minecraft.player == null || reach <= 0.0F) {
			return null;
		}

		Vec3 from = camera.getEyePosition(partialTick);
		Vec3 direction = camera.getViewVector(partialTick);
		double range = reach;
		double rangeSq = Mth.square(range);
		Vec3 lookEnd = from.add(direction.scale(range));
		BlockHitResult blockHit = camera.level().clipIncludingBorder(
			new ClipContext(from, lookEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, camera)
		);
		if (blockHit.getType() != HitResult.Type.MISS) {
			rangeSq = Math.min(rangeSq, blockHit.getLocation().distanceToSqr(from));
			range = Math.sqrt(rangeSq);
		}

		Vec3 to = from.add(direction.scale(range));
		AABB box = camera.getBoundingBox().expandTowards(direction.scale(range)).inflate(1.0, 1.0, 1.0);
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
			camera,
			from,
			to,
			box,
			entity -> pickable(minecraft.player, entity),
			rangeSq
		);
		if (entityHit == null) {
			return null;
		}

		Entity picked = entityHit.getEntity();
		if (picked instanceof EnderDragonPart part) {
			picked = part.parentMob;
		}

		if (!(picked instanceof LivingEntity living) || living == minecraft.player) {
			return null;
		}

		return living;
	}

	private static boolean pickable(Entity viewer, Entity entity) {
		if (entity == viewer || !EntitySelector.CAN_BE_PICKED.test(entity)) {
			return false;
		}

		return entity instanceof LivingEntity || entity instanceof EnderDragonPart;
	}
}
