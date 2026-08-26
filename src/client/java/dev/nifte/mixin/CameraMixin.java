package dev.nifte.mixin;

import net.minecraft.client.Camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.zoom.ZoomFeature;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
	private void nifte$zoom(float partialTicks, CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(ZoomFeature.modifyFov(cir.getReturnValue()));
	}
}
