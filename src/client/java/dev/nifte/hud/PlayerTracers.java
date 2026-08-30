package dev.nifte.hud;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import dev.nifte.config.NifteConfig;

public final class PlayerTracers {
	public static final int RANGE_MIN = 1;
	public static final int RANGE_MAX = 256;
	public static final int RANGE_DEFAULT = 64;

	private static final int COLOR = ARGB.color(255, 255, 255);
	private static final float LINE_WIDTH = 3.0F;

	private PlayerTracers() {
	}

	public static void toggle() {
		NifteConfig config = NifteConfig.get();
		config.playerTracersEnabled = !config.playerTracersEnabled;
		NifteConfig.save();

		ToggleOverlay.show("nifte.player_tracers.toggle", config.playerTracersEnabled);
	}

	public static void emit(Minecraft minecraft, Camera camera, float partialTick) {
		NifteConfig config = NifteConfig.get();
		if (!config.playerTracersEnabled || minecraft.gui.hud.isHidden()) {
			return;
		}

		LocalPlayer self = minecraft.player;
		if (self == null || minecraft.level == null) {
			return;
		}

		Vec3 start = PlayerTracerGeometry.cursor(camera);
		double rangeSq = Mth.square(config.playerTracersRange);
		for (Player player : minecraft.level.players()) {
			if (!PlayerTracerGeometry.visible(self, player, rangeSq)) {
				continue;
			}

			Gizmos.line(start, PlayerTracerGeometry.target(camera, player, partialTick), COLOR, LINE_WIDTH).setAlwaysOnTop();
		}
	}
}
