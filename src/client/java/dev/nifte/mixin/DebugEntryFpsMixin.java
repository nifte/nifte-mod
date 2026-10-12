package dev.nifte.mixin;

import java.util.Locale;

import net.minecraft.client.gui.components.debug.DebugEntryFps;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.nifte.feature.fps.FpsCap;

@Mixin(DebugEntryFps.class)
public abstract class DebugEntryFpsMixin {
	@Redirect(
		method = "display",
		at = @At(value = "INVOKE", target = "Ljava/lang/String;format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;")
	)
	private String nifte$unlimitedFps(Locale locale, String template, Object[] args) {
		if (args.length > 1 && args[1] instanceof Integer limit && FpsCap.isUnlimited(limit)) {
			args[1] = "inf";
		}

		return String.format(locale, template, args);
	}
}
