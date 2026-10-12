package dev.nifte.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.ProfilerFiller;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.nifte.feature.fps.FpsCap;

@Mixin(Minecraft.class)
public abstract class FpsLimitMixin {
	@Redirect(
		method = "runTick(Z)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V")
	)
	private void nifte$limitHighFps(ProfilerFiller profiler, String name) {
		profiler.popPush(name);
		if ("frameLimiter".equals(name)) {
			FpsCap.limitAboveVanilla((Minecraft) (Object) this);
		}
	}
}
