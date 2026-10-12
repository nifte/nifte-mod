package dev.nifte.mixin;

import net.minecraft.client.gui.components.debugchart.FpsDebugChart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import dev.nifte.feature.fps.FpsCap;

@Mixin(FpsDebugChart.class)
public abstract class FpsDebugChartMixin {
	@ModifyConstant(method = "extractAdditionalLinesAndLabels", constant = @Constant(intValue = 250))
	private int nifte$fpsChartCap(int original) {
		return FpsCap.MAX_FPS;
	}
}
