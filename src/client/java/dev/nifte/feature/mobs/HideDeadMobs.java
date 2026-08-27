package dev.nifte.feature.mobs;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import dev.nifte.config.NifteConfig;

public final class HideDeadMobs {
	private HideDeadMobs() {
	}

	public static boolean shouldHide(Entity entity) {
		return NifteConfig.get().hideDeadMobs && entity instanceof Mob mob && mob.isDeadOrDying();
	}
}
