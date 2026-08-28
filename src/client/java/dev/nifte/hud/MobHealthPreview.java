package dev.nifte.hud;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

final class MobHealthPreview {
	private static final float BODY_YAW = 210.0F;
	private static final float HEAD_YAW = 30.0F;
	private static final float HEIGHT_PADDING = 1.45F;
	private static final float WIDTH_PADDING = 1.55F;
	private static final float OFFSET_Y = 0.08F;

	private MobHealthPreview() {
	}

	static void extract(GuiGraphicsExtractor graphics, LivingEntity entity, float partialTick, int x0, int y0, int x1, int y1) {
		EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		if (dispatcher.getRenderer(entity) == null) {
			return;
		}

		EntityRenderState renderState = dispatcher.extractEntity(entity, partialTick);
		renderState.shadowPieces.clear();
		renderState.outlineColor = 0;
		renderState.displayFireAnimation = false;
		renderState.nameTag = null;
		renderState.scoreText = null;
		renderState.isInvisible = false;
		if (renderState instanceof LivingEntityRenderState living) {
			living.bodyRot = BODY_YAW;
			living.yRot = HEAD_YAW;
			living.xRot = 0.0F;
			living.pose = Pose.STANDING;
			living.bedOrientation = null;
			living.isInvisibleToPlayer = false;
			living.hasRedOverlay = false;
			living.deathTime = 0.0F;
			if (living.scale != 0.0F) {
				living.boundingBoxWidth /= living.scale;
				living.boundingBoxHeight /= living.scale;
				living.scale = 1.0F;
			}
		}

		float width = Math.max(renderState.boundingBoxWidth * WIDTH_PADDING, 0.1F);
		float height = Math.max(renderState.boundingBoxHeight * HEIGHT_PADDING, 0.1F);
		float size = Math.min((x1 - x0) / width, (y1 - y0) / height);
		Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F + OFFSET_Y, 0.0F);
		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf camera = new Quaternionf();
		rotation.mul(camera);
		graphics.entity(renderState, size, translation, rotation, camera, x0, y0, x1, y1);
	}
}
