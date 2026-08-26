package dev.nifte.feature.bridge;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;

public final class BedrockBridging {
	private static final double EDGE = 0.25;
	private static final double GRAZE = 0.35;
	private static final double FACE_INSET = 0.45;

	private BedrockBridging() {
	}

	public static BlockHitResult adjust(LocalPlayer player, InteractionHand hand, BlockHitResult hit) {
		BridgePlacement placement = find(player, hand, hit);
		return placement == null ? hit : placement.hit();
	}

	public static @Nullable BlockHitResult airHit(LocalPlayer player, InteractionHand hand, @Nullable HitResult vanilla) {
		if (vanilla != null && vanilla.getType() == HitResult.Type.BLOCK) {
			return null;
		}

		BridgePlacement placement = find(player, hand, vanilla);
		return placement == null ? null : placement.hit();
	}

	public static @Nullable BlockPos previewTarget(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return null;
		}

		for (InteractionHand hand : InteractionHand.values()) {
			BridgePlacement placement = find(player, hand, minecraft.hitResult);
			if (placement != null) {
				return placement.target();
			}
		}

		return null;
	}

	private static @Nullable BridgePlacement find(LocalPlayer player, InteractionHand hand, @Nullable HitResult vanilla) {
		if (!NifteConfig.get().bedrockBridgingEnabled || player.isSpectator() || !(player.getItemInHand(hand).getItem() instanceof BlockItem)) {
			return null;
		}

		Vec3 from = player.getEyePosition();
		Vec3 look = player.getViewVector(1.0F);
		double reach = player.blockInteractionRange();
		Vec3 to = from.add(look.scale(reach));
		BridgePlacement alongRay = scan(player, hand, from, to, look);
		if (alongRay == null) {
			return null;
		}

		if (vanilla instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK && shouldKeepVanillaHit(player, blockHit, from, to, look, alongRay)) {
			return null;
		}

		return alongRay;
	}

	private static boolean shouldKeepVanillaHit(
		LocalPlayer player,
		BlockHitResult vanilla,
		Vec3 from,
		Vec3 to,
		Vec3 look,
		BridgePlacement alongRay
	) {
		if (isHiddenLookFace(vanilla.getDirection(), from, look, vanilla.getBlockPos())) {
			return false;
		}

		BlockPos vanillaTarget = vanillaPlacementTarget(player.level(), vanilla);
		if (!player.level().getBlockState(vanillaTarget).canBeReplaced()) {
			return false;
		}

		return vanillaTarget.equals(alongRay.target()) || !new AABB(vanillaTarget).clip(from, to).isEmpty();
	}

	private static BlockPos vanillaPlacementTarget(Level level, BlockHitResult hit) {
		BlockPos clicked = hit.getBlockPos();
		return level.getBlockState(clicked).canBeReplaced() ? clicked : clicked.relative(hit.getDirection());
	}

	private static @Nullable BridgePlacement scan(LocalPlayer player, InteractionHand hand, Vec3 from, Vec3 to, Vec3 look) {
		Level level = player.level();
		CollisionContext context = CollisionContext.of(player);
		AABB sweep = new AABB(from, to).inflate(GRAZE + 1.0);
		BridgePlacement best = null;
		for (BlockPos cursor : BlockPos.betweenClosed(
			BlockPos.containing(sweep.minX, sweep.minY, sweep.minZ),
			BlockPos.containing(sweep.maxX, sweep.maxY, sweep.maxZ)
		)) {
			BlockPos support = cursor.immutable();
			if (level.getBlockState(support).canBeReplaced()) {
				continue;
			}

			VoxelShape shape = level.getBlockState(support).getShape(level, support, context);
			if (shape.isEmpty()) {
				continue;
			}

			List<AABB> parts = shape.toAabbs();
			BridgePlacement candidate = intersectingWrap(player, hand, support, parts, from, to, look);
			if (candidate == null) {
				candidate = graze(player, hand, support, parts, from, to);
			}

			if (candidate != null && (best == null || candidate.distanceSq() < best.distanceSq())) {
				best = candidate;
			}
		}

		return best;
	}

	private static @Nullable BridgePlacement intersectingWrap(
		LocalPlayer player,
		InteractionHand hand,
		BlockPos support,
		List<AABB> parts,
		Vec3 from,
		Vec3 to,
		Vec3 look
	) {
		BlockHitResult direct = AABB.clip(parts, from, to, support);
		if (direct == null) {
			return null;
		}

		Direction wrap = wrapFace(direct, look);
		if (wrap == null) {
			return null;
		}

		return placement(player, hand, support, wrap, from);
	}

	private static @Nullable Direction wrapFace(BlockHitResult hit, Vec3 look) {
		if (hit.isInside()) {
			return null;
		}

		return edgeFace(hit.getDirection(), hit.getBlockPos(), hit.getLocation(), look);
	}

	private static @Nullable BridgePlacement graze(LocalPlayer player, InteractionHand hand, BlockPos support, List<AABB> parts, Vec3 from, Vec3 to) {
		if (AABB.clip(parts, from, to, support) != null || AABB.clip(inflated(parts, GRAZE), from, to, support) == null) {
			return null;
		}

		BridgePlacement best = null;
		for (Direction face : Direction.values()) {
			BridgePlacement candidate = placement(player, hand, support, face, from);
			if (candidate == null || new AABB(candidate.target()).clip(from, to).isEmpty()) {
				continue;
			}

			if (best == null || candidate.distanceSq() < best.distanceSq()) {
				best = candidate;
			}
		}

		return best;
	}

	private static @Nullable BridgePlacement placement(LocalPlayer player, InteractionHand hand, BlockPos support, Direction face, Vec3 from) {
		Level level = player.level();
		if (!isHiddenLookFace(face, from, player.getViewVector(1.0F), support)) {
			return null;
		}

		BlockPos target = support.relative(face);
		if (target.equals(BlockPos.containing(from))
			|| !level.getWorldBorder().isWithinBounds(target)
			|| !player.isWithinBlockInteractionRange(support, 1.0)
			|| !player.isWithinBlockInteractionRange(target, 0.0)
			|| !level.getBlockState(target).canBeReplaced()
			|| hasVisibleVanillaSupport(player, target)) {
			return null;
		}

		BlockHitResult hit = new BlockHitResult(faceHit(support, face), face, support, false);
		if (!canPredictPlace(player, hand, hit)) {
			return null;
		}

		return new BridgePlacement(hit, target, from.distanceToSqr(Vec3.atCenterOf(target)));
	}

	private static boolean canPredictPlace(LocalPlayer player, InteractionHand hand, BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(stack.getItem() instanceof BlockItem blockItem) || !player.mayUseItemAt(hit.getBlockPos(), hit.getDirection(), stack)) {
			return false;
		}

		BlockPlaceContext context = new BlockPlaceContext(player, hand, stack, hit);
		if (!context.canPlace()) {
			return false;
		}

		BlockState state = blockItem.getBlock().getStateForPlacement(context);
		BlockPos pos = context.getClickedPos();
		return state != null
			&& state.canSurvive(player.level(), pos)
			&& player.level().isUnobstructed(state, pos, CollisionContext.placementContext(player));
	}

	private static boolean hasVisibleVanillaSupport(LocalPlayer player, BlockPos target) {
		Level level = player.level();
		Vec3 eye = player.getEyePosition();
		for (Direction towardNeighbor : Direction.values()) {
			BlockPos neighbor = target.relative(towardNeighbor);
			if (level.getBlockState(neighbor).canBeReplaced() || !player.isWithinBlockInteractionRange(neighbor, 1.0)) {
				continue;
			}

			if (isFaceVisible(towardNeighbor.getOpposite(), eye, neighbor)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isFaceVisible(Direction face, Vec3 eye, BlockPos block) {
		Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
		return eye.subtract(faceCenter(block, face)).dot(normal) > 0.0;
	}

	private static boolean isHiddenLookFace(Direction face, Vec3 eye, Vec3 look, BlockPos support) {
		Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
		if (look.dot(normal) <= 0.0) {
			return false;
		}

		return eye.subtract(faceCenter(support, face)).dot(normal) < 0.0;
	}

	private static List<AABB> inflated(List<AABB> parts, double amount) {
		List<AABB> inflated = new ArrayList<>(parts.size());
		for (AABB part : parts) {
			inflated.add(part.inflate(amount));
		}

		return inflated;
	}

	private static @Nullable Direction edgeFace(Direction clicked, BlockPos pos, Vec3 location, Vec3 look) {
		double x = Mth.clamp(location.x - pos.getX(), 0.0, 1.0);
		double y = Mth.clamp(location.y - pos.getY(), 0.0, 1.0);
		double z = Mth.clamp(location.z - pos.getZ(), 0.0, 1.0);
		Direction best = null;
		double bestDist = EDGE;
		double bestAlign = Double.NEGATIVE_INFINITY;
		for (Direction dir : Direction.values()) {
			if (dir.getAxis() == clicked.getAxis()) {
				continue;
			}

			double coord = switch (dir.getAxis()) {
				case X -> x;
				case Y -> y;
				case Z -> z;
			};
			double dist = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - coord : coord;
			if (dist > EDGE) {
				continue;
			}

			double align = dir.getStepX() * look.x + dir.getStepY() * look.y + dir.getStepZ() * look.z;
			if (best == null || dist < bestDist - 1.0E-4 || (dist <= bestDist && align > bestAlign)) {
				best = dir;
				bestDist = dist;
				bestAlign = align;
			}
		}

		return best;
	}

	private static Vec3 faceHit(BlockPos pos, Direction face) {
		return Vec3.atCenterOf(pos).add(face.getStepX() * FACE_INSET, face.getStepY() * FACE_INSET, face.getStepZ() * FACE_INSET);
	}

	private static Vec3 faceCenter(BlockPos pos, Direction face) {
		return Vec3.atCenterOf(pos).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
	}
}
