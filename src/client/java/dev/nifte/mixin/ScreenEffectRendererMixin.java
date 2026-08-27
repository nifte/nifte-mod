package dev.nifte.mixin;

import net.minecraft.client.renderer.ScreenEffectRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import dev.nifte.feature.overlay.FireOverlayFeature;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
	@ModifyConstant(
		method = "lambda$submitFire$0(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V",
		constant = @Constant(floatValue = -0.3F)
	)
	private static float nifte$lowerFireOverlay(float original) {
		return original - FireOverlayFeature.yOffset();
	}
}
