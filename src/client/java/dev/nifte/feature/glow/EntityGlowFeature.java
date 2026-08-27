package dev.nifte.feature.glow;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import dev.nifte.config.NifteConfig;
import dev.nifte.hud.ToggleOverlay;

public final class EntityGlowFeature {
	private EntityGlowFeature() {
	}

	public static void toggleHostileMobs() {
		NifteConfig config = NifteConfig.get();
		config.highlightHostileMobs = !config.highlightHostileMobs;
		NifteConfig.save();

		ToggleOverlay.show("nifte.highlight_hostile_mobs.toggle", config.highlightHostileMobs);
	}

	public static void toggleOtherPlayers() {
		NifteConfig config = NifteConfig.get();
		config.highlightOtherPlayers = !config.highlightOtherPlayers;
		NifteConfig.save();

		ToggleOverlay.show("nifte.highlight_other_players.toggle", config.highlightOtherPlayers);
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
