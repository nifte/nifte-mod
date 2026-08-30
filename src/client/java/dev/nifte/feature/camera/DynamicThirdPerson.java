package dev.nifte.feature.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;

public final class DynamicThirdPerson {
	private static final float TURN_SCALE = 0.15F;

	private static float cameraYaw;
	private static float cameraPitch;
	private static boolean controlling;
	private static LocalPlayer controlledPlayer;

	private DynamicThirdPerson() {
	}

	public static boolean isActive() {
		if (!NifteConfig.get().dynamicThirdPerson) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || player.isSpectator() || player.isSleeping() || player.isPassenger()) {
			return false;
		}

		if (minecraft.options.getCameraType().isFirstPerson()) {
			return false;
		}

		return minecraft.getCameraEntity() == player;
	}

	public static float viewYaw(Entity entity, float partialTicks) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (!isActive() || player == null) {
			release();
			return entity.getViewYRot(partialTicks);
		}

		capture(player);
		if (followLookWhileGliding(player)) {
			syncCameraToPlayer(player);
			return entity.getViewYRot(partialTicks);
		}

		return cameraYaw;
	}

	public static float viewPitch(Entity entity, float partialTicks) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (!isActive() || player == null) {
			release();
			return entity.getViewXRot(partialTicks);
		}

		capture(player);
		if (followLookWhileGliding(player)) {
			syncCameraToPlayer(player);
			return entity.getViewXRot(partialTicks);
		}

		return cameraPitch;
	}

	public static boolean turnCamera(double yawInput, double pitchInput) {
		if (!isActive()) {
			return false;
		}

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || followLookWhileGliding(player)) {
			return false;
		}

		capture(player);
		cameraYaw += (float) yawInput * TURN_SCALE;
		cameraPitch = Mth.clamp(cameraPitch + (float) pitchInput * TURN_SCALE, -90.0F, 90.0F);
		player.setXRot(cameraPitch);
		return true;
	}

	public static @Nullable RemappedInput apply(LocalPlayer player) {
		if (!isActive() || player != Minecraft.getInstance().player) {
			release();
			return null;
		}

		capture(player);
		if (followLookWhileGliding(player)) {
			syncCameraToPlayer(player);
			return null;
		}

		player.setXRot(cameraPitch);

		Vec2 move = player.input.getMoveVector();
		if (move.lengthSquared() < 1.0E-6F) {
			return null;
		}

		float targetYaw = cameraYaw - (float) Mth.atan2(move.x, move.y) * (180.0F / (float) Math.PI);
		player.setYRot(targetYaw);
		player.setYHeadRot(targetYaw);

		Input previous = player.input.keyPresses;
		return new RemappedInput(
			new Vec2(0.0F, move.length()),
			new Input(true, false, false, false, previous.jump(), previous.shift(), previous.sprint())
		);
	}

	private static boolean followLookWhileGliding(LocalPlayer player) {
		return player.isFallFlying() && player.input.getMoveVector().lengthSquared() < 1.0E-6F;
	}

	private static void syncCameraToPlayer(LocalPlayer player) {
		cameraYaw = player.getYRot();
		cameraPitch = player.getXRot();
	}

	private static void capture(LocalPlayer player) {
		if (controlling && controlledPlayer == player) {
			return;
		}

		cameraYaw = player.getYRot();
		cameraPitch = player.getXRot();
		controlledPlayer = player;
		controlling = true;
	}

	private static void release() {
		if (!controlling) {
			return;
		}

		LocalPlayer player = controlledPlayer;
		if (player != null && player == Minecraft.getInstance().player) {
			player.setYRot(cameraYaw);
			player.setXRot(cameraPitch);
			player.yRotO = cameraYaw;
			player.xRotO = cameraPitch;
			player.setYHeadRot(cameraYaw);
		}

		controlling = false;
		controlledPlayer = null;
	}

	public record RemappedInput(Vec2 move, Input keys) {
	}
}
