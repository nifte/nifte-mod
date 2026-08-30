package dev.nifte.hud;

import java.util.List;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.Vec3;

final class HudLines {
	private HudLines() {
	}

	static void segment(Minecraft minecraft, Camera camera, Vec3 start, Vec3 end, int color, float pixelWidth, boolean alwaysOnTop) {
		polyline(minecraft, camera, List.of(start, end), color, pixelWidth, alwaysOnTop);
	}

	static void polyline(Minecraft minecraft, Camera camera, List<Vec3> points, int color, float pixelWidth, boolean alwaysOnTop) {
		HudLineGeometry.Rails rails = HudLineGeometry.rails(
			camera,
			points,
			pixelWidth,
			Math.max(1, minecraft.getWindow().getHeight())
		);
		if (rails == null) {
			return;
		}

		GizmoStyle style = GizmoStyle.fill(color);
		Vec3[] left = rails.left();
		Vec3[] right = rails.right();
		for (int i = 1; i < left.length; i++) {
			GizmoProperties properties = Gizmos.rect(left[i - 1], right[i - 1], right[i], left[i], style);
			if (alwaysOnTop) {
				properties.setAlwaysOnTop();
			}
		}
	}
}
