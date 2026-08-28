package dev.nifte.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.hud.CompassHud;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {
	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void nifte$offsetBossBars(GuiGraphicsExtractor graphics, CallbackInfo ci) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(0, CompassHud.bossBarOffset());
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void nifte$restoreBossBars(GuiGraphicsExtractor graphics, CallbackInfo ci) {
		graphics.pose().popMatrix();
	}
}
