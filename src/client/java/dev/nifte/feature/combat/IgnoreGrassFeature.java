package dev.nifte.feature.combat;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;

public final class IgnoreGrassFeature {
	private IgnoreGrassFeature() {
	}

	public static @Nullable HitResult hitThroughGrass(LocalPlayer player, Entity camera, @Nullable HitResult current, float partialTick) {
		if (!NifteConfig.get().ignoreGrassInCombat || player.isSpectator()) {
			return null;
		}

		if (!(current instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
			return null;
		}

		if (!isIgnoredPlant(camera.level(), blockHit.getBlockPos(), camera)) {
			return null;
		}

		return pickEntity(player, camera, partialTick);
	}

	private static boolean isIgnoredPlant(Level level, BlockPos pos, Entity camera) {
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || !state.getFluidState().isEmpty()) {
			return false;
		}

		return state.canBeReplaced() && state.getCollisionShape(level, pos, CollisionContext.of(camera)).isEmpty();
	}

	private static @Nullable EntityHitResult pickEntity(LocalPlayer player, Entity camera, float partialTick) {
		AttackRange attackRange = player.getAttackRangeWith(player.getActiveItem());
		double entityRange = attackRange.effectiveMaxRange(player);
		Vec3 from = camera.getEyePosition(partialTick);
		Vec3 direction = camera.getViewVector(partialTick);
		double searchDistance = entityRange;
		double searchDistanceSq = Mth.square(entityRange);
		Vec3 lookEnd = from.add(direction.scale(Math.max(entityRange, player.blockInteractionRange())));
		BlockHitResult colliderHit = camera.level().clipIncludingBorder(
			new ClipContext(from, lookEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, camera)
		);
		if (colliderHit.getType() != HitResult.Type.MISS) {
			searchDistanceSq = Math.min(searchDistanceSq, colliderHit.getLocation().distanceToSqr(from));
			searchDistance = Math.sqrt(searchDistanceSq);
		}

		Vec3 to = from.add(direction.scale(searchDistance));
		AABB box = camera.getBoundingBox().expandTowards(direction.scale(searchDistance)).inflate(1.0, 1.0, 1.0);
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
			camera,
			from,
			to,
			box,
			EntitySelector.CAN_BE_PICKED,
			searchDistanceSq
		);
		if (entityHit == null || !attackRange.isInRange(player, entityHit.getLocation())) {
			return null;
		}

		return entityHit;
	}
}
