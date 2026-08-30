package dev.nifte.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.config.NifteConfigPreview;

@Mixin(targets = "me.shedaniel.clothconfig2.gui.AbstractConfigScreen$QuitSaveConsumer", remap = false)
public abstract class AbstractConfigScreenQuitSaveConsumerMixin {
	@Inject(method = "accept", at = @At("HEAD"))
	private void nifte$revertUnsavedOnDiscard(boolean quit, CallbackInfo ci) {
		if (quit) {
			NifteConfigPreview.discard();
		}
	}
}
