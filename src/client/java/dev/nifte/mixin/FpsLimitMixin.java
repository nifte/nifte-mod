package dev.nifte.mixin;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import dev.nifte.feature.fps.FpsCap;

@Mixin(Minecraft.class)
public abstract class FpsLimitMixin {
	@ModifyConstant(method = "runTick", constant = @Constant(intValue = 260))
	private int nifte$fpsCap(int original) {
		return FpsCap.UNLIMITED;
	}
}
