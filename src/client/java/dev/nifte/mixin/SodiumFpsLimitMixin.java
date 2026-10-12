package dev.nifte.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import dev.nifte.feature.fps.FpsCap;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.SodiumConfigBuilder", remap = false)
public abstract class SodiumFpsLimitMixin {
	@ModifyArgs(
		method = "buildGeneralPage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/caffeinemc/mods/sodium/api/config/structure/IntegerOptionBuilder;setRange(III)Lnet/caffeinemc/mods/sodium/api/config/structure/IntegerOptionBuilder;"
		),
		remap = false
	)
	private void nifte$raiseFpsSlider(Args args) {
		int min = (Integer) args.get(0);
		int max = (Integer) args.get(1);
		int step = (Integer) args.get(2);
		if (min == 10 && max == FpsCap.VANILLA_UNLIMITED && step == 10) {
			args.set(1, FpsCap.UNLIMITED);
		}
	}
}
