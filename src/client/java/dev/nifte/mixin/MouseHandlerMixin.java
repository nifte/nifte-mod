package dev.nifte.mixin;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.camera.DynamicThirdPerson;
import dev.nifte.feature.zoom.ZoomFeature;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Shadow
	private double accumulatedDX;

	@Shadow
	private double accumulatedDY;

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void nifte$zoomScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
		if (ZoomFeature.handleScroll(yoffset)) {
			ci.cancel();
		}
	}

	@Inject(method = "turnPlayer", at = @At("HEAD"))
	private void nifte$zoomSensitivity(double mousea, CallbackInfo ci) {
		double multiplier = ZoomFeature.sensitivityMultiplier();
		if (multiplier != 1.0) {
			this.accumulatedDX *= multiplier;
			this.accumulatedDY *= multiplier;
		}
	}

	@Redirect(
		method = "turnPlayer",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V")
	)
	private void nifte$dynamicThirdPersonTurn(LocalPlayer player, double yawInput, double pitchInput) {
		if (!DynamicThirdPerson.turnCamera(yawInput, pitchInput)) {
			player.turn(yawInput, pitchInput);
		}
	}
}
