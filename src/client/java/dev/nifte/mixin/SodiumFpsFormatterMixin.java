package dev.nifte.mixin;

import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.fps.FpsCap;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatterImpls", remap = false)
public abstract class SodiumFpsFormatterMixin {
	@Inject(method = "lambda$fpsLimit$0", at = @At("HEAD"), cancellable = true, remap = false)
	private static void nifte$formatFpsLimit(int value, CallbackInfoReturnable<Component> callback) {
		if (value >= FpsCap.UNLIMITED) {
			callback.setReturnValue(Component.translatable("options.framerateLimit.max"));
			return;
		}

		callback.setReturnValue(Component.translatable("options.framerate", value));
	}
}
