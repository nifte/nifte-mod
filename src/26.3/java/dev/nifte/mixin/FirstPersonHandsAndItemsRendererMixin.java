package dev.nifte.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.overlay.HideHeldTotemFeature;
import dev.nifte.feature.overlay.ShieldOverlayFeature;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsAndItemsRendererMixin {
	@Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
	private void nifte$hideHeldTotem(
		PlayerRenderState playerState,
		FirstPersonHandsAndItemsRenderState hands,
		float frameInterp,
		float xRot,
		InteractionHand hand,
		float attack,
		ItemStack itemStack,
		float inverseArmHeight,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		CallbackInfo ci
	) {
		if (HideHeldTotemFeature.shouldHide(itemStack)) {
			ci.cancel();
		}
	}

	@Inject(
		method = "submitArmWithItem",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER)
	)
	private void nifte$lowerShield(
		PlayerRenderState playerState,
		FirstPersonHandsAndItemsRenderState hands,
		float frameInterp,
		float xRot,
		InteractionHand hand,
		float attack,
		ItemStack itemStack,
		float inverseArmHeight,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		CallbackInfo ci
	) {
		ShieldOverlayFeature.apply(poseStack, itemStack);
	}
}
