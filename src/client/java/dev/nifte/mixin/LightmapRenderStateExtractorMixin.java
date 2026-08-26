package dev.nifte.mixin;

import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.fullbright.FullbrightFeature;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {
	@Inject(method = "extract", at = @At("TAIL"))
	private void nifte$fullbright(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
		if (FullbrightFeature.isActive()) {
			renderState.brightness = 1.0F;
			renderState.nightVisionEffectIntensity = 1.0F;
			renderState.darknessEffectScale = 0.0F;
		}
	}
}
