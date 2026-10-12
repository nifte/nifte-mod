package dev.nifte.mixin;

import net.minecraft.client.gui.components.debug.DebugEntryFps;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import dev.nifte.feature.fps.FpsCap;

@Mixin(DebugEntryFps.class)
public abstract class DebugEntryFpsMixin {
	@ModifyConstant(method = "display", constant = @Constant(intValue = 260))
	private int nifte$unlimitedFps(int original) {
		return FpsCap.UNLIMITED;
	}
}
