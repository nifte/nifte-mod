package dev.nifte.feature.camera;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import dev.nifte.config.NifteConfig;

public final class AutoThirdPerson {
	private static boolean held;
	private static boolean wasEligible;

	private AutoThirdPerson() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(AutoThirdPerson::tick);
	}

	private static void tick(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (!NifteConfig.get().autoThirdPerson || player == null) {
			restoreIfHeld(minecraft);
			wasEligible = false;
			return;
		}

		boolean eligible = shouldUseThirdPerson(player);
		CameraType current = minecraft.options.getCameraType();
		if (eligible) {
			if (!wasEligible && current.isFirstPerson()) {
				setCameraType(minecraft, CameraType.THIRD_PERSON_BACK);
				held = true;
			} else if (held && current.isFirstPerson()) {
				held = false;
			}
		} else {
			restoreIfHeld(minecraft);
		}

		wasEligible = eligible;
	}

	private static boolean shouldUseThirdPerson(LocalPlayer player) {
		if (player.isSpectator() || player.isSleeping()) {
			return false;
		}

		return player.isPassenger() || player.isFallFlying() || player.getAbilities().flying;
	}

	private static void restoreIfHeld(Minecraft minecraft) {
		if (!held) {
			return;
		}

		held = false;
		if (!minecraft.options.getCameraType().isFirstPerson()) {
			setCameraType(minecraft, CameraType.FIRST_PERSON);
		}
	}

	private static void setCameraType(Minecraft minecraft, CameraType cameraType) {
		CameraType previous = minecraft.options.getCameraType();
		if (previous == cameraType) {
			return;
		}

		minecraft.options.setCameraType(cameraType);
		if (previous.isFirstPerson() != cameraType.isFirstPerson()) {
			minecraft.gameRenderer.checkEntityPostEffect(cameraType.isFirstPerson() ? minecraft.getCameraEntity() : null);
		}
	}
}
