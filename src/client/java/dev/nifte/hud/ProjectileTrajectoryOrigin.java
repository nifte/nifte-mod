package dev.nifte.hud;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;

final class ProjectileTrajectoryOrigin {
	private static final float SPAWN_EYE_OFFSET = 0.1F;
	private static final float VISUAL_DEPTH = 0.7F;
	private static final float THROW_X = 0.56F;
	private static final float THROW_Y = -0.38F;
	private static final float AIM_X = 0.40F;
	private static final float AIM_Y = -0.16F;

	private ProjectileTrajectoryOrigin() {
	}

	static Vec3 spawn(LocalPlayer player, float partialTick) {
		Vec3 eye = player.getEyePosition(partialTick);
		return new Vec3(eye.x, eye.y - SPAWN_EYE_OFFSET, eye.z);
	}

	static Vec3 visual(Minecraft minecraft, Camera camera, LocalPlayer player, InteractionHand hand, ItemStack stack, float partialTick) {
		if (!minecraft.options.getCameraType().isFirstPerson()) {
			return spawn(player, partialTick);
		}

		HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
		float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		boolean aiming = stack.getItem() instanceof BowItem
			|| stack.getItem() instanceof TridentItem
			|| stack.getItem() instanceof CrossbowItem;
		float ndcX = side * (aiming ? AIM_X : THROW_X);
		float ndcY = aiming ? AIM_Y : THROW_Y;
		Vec3 near = camera.getNearPlane(camera.getFov()).getPointOnPlane(ndcX, ndcY);
		double length = near.length();
		if (length < 1.0E-4) {
			return spawn(player, partialTick);
		}

		return camera.position().add(near.scale(VISUAL_DEPTH / length));
	}
}
