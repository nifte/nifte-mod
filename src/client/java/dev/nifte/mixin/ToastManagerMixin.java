package dev.nifte.mixin;

import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.config.NifteConfig;

@Mixin(ToastManager.class)
public abstract class ToastManagerMixin {
	@Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
	private void nifte$filterToasts(Toast toast, CallbackInfo ci) {
		NifteConfig config = NifteConfig.get();
		if (config.disableAdvancementToasts && toast instanceof AdvancementToast) {
			ci.cancel();
			return;
		}

		if (config.disableRecipeToasts && toast instanceof RecipeToast) {
			ci.cancel();
		}
	}
}
