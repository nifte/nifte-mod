package dev.nifte.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.overlay.HideHeldTotemFeature;
import dev.nifte.feature.overlay.ShieldOverlayFeature;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
	private void nifte$hideHeldTotem(
		AbstractClientPlayer player,
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
		AbstractClientPlayer player,
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
