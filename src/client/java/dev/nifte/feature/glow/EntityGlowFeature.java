package dev.nifte.feature.glow;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import dev.nifte.config.NifteConfig;

public final class EntityGlowFeature {
	private EntityGlowFeature() {
	}

	public static boolean shouldGlow(Entity entity) {
		if (!entity.isAlive()) {
			return false;
		}

		NifteConfig config = NifteConfig.get();
		if (config.highlightHostileMobs && entity instanceof Enemy) {
			return true;
		}

		return config.highlightOtherPlayers
			&& entity instanceof Player player
			&& !(player instanceof LocalPlayer)
			&& !player.isSpectator();
	}
}
