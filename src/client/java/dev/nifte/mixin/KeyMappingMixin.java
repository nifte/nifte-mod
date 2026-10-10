package dev.nifte.mixin;

import net.minecraft.client.KeyMapping;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.food.QuickEatFeature;
import dev.nifte.feature.quickuse.QuickUseFeature;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {
	@Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
	private void nifte$emulateUse(CallbackInfoReturnable<Boolean> cir) {
		KeyMapping mapping = (KeyMapping) (Object) this;
		if (QuickUseFeature.shouldEmulateUse(mapping) || QuickEatFeature.shouldEmulateUse(mapping)) {
			cir.setReturnValue(true);
		}
	}
}
