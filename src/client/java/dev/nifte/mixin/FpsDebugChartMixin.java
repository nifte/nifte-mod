package dev.nifte.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debugchart.FpsDebugChart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.fps.FpsCap;

@Mixin(FpsDebugChart.class)
public abstract class FpsDebugChartMixin {
	@Shadow
	protected abstract int getSampleHeight(double nanos);

	@Inject(method = "extractAdditionalLinesAndLabels", at = @At("TAIL"))
	private void nifte$highFpsLine(GuiGraphicsExtractor graphics, int left, int width, int bottom, CallbackInfo ci) {
		int limit = Minecraft.getInstance().options.framerateLimit().get();
		if (limit <= 250 || limit > FpsCap.MAX_FPS) {
			return;
		}

		graphics.horizontalLine(left, left + width - 1, bottom - this.getSampleHeight(1.0E9 / limit) - 1, 0xFF00FFFF);
	}
}
