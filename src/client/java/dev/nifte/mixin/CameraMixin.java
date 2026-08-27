package dev.nifte.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.camera.DynamicThirdPerson;
import dev.nifte.feature.zoom.ZoomFeature;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
	private void nifte$zoom(float partialTicks, CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(ZoomFeature.modifyFov(cir.getReturnValue()));
	}

	@Redirect(
		method = "alignWithEntity",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F")
	)
	private float nifte$dynamicThirdPersonYaw(Entity entity, float partialTicks) {
		return DynamicThirdPerson.viewYaw(entity, partialTicks);
	}

	@Redirect(
		method = "alignWithEntity",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F")
	)
	private float nifte$dynamicThirdPersonPitch(Entity entity, float partialTicks) {
		return DynamicThirdPerson.viewPitch(entity, partialTicks);
	}
}
