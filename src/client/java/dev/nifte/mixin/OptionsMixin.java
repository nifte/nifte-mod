package dev.nifte.mixin;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.fps.FpsCap;

@Mixin(net.minecraft.client.Options.class)
public abstract class OptionsMixin {
	@Shadow
	@Final
	@Mutable
	private OptionInstance<Integer> framerateLimit;

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;load()V"))
	private void nifte$raiseFpsCap(Minecraft minecraft, File workingDirectory, CallbackInfo ci) {
		this.framerateLimit = FpsCap.framerateLimit();
	}
}
