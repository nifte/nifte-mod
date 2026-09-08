package dev.nifte.feature.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import dev.nifte.config.NifteConfig;

public final class FirstPersonEffectParticles {
	private FirstPersonEffectParticles() {
	}

	public static boolean shouldHide(LivingEntity entity) {
		if (!NifteConfig.get().hideFirstPersonEffectParticles) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		Entity camera = minecraft.getCameraEntity();
		return camera == entity && minecraft.options.getCameraType().isFirstPerson();
	}
}
