package dev.nifte.mixin;

import net.minecraft.client.CameraType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.config.NifteConfig;

@Mixin(CameraType.class)
public abstract class CameraTypeMixin {
	@Inject(method = "cycle", at = @At("RETURN"), cancellable = true)
	private void nifte$skipFront(CallbackInfoReturnable<CameraType> cir) {
		if (NifteConfig.get().disableFrontThirdPerson && cir.getReturnValue() == CameraType.THIRD_PERSON_FRONT) {
			cir.setReturnValue(CameraType.FIRST_PERSON);
		}
	}
}
