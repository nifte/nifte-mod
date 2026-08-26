package dev.nifte.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;

import dev.nifte.config.NifteConfig;
import dev.nifte.feature.ping.NumericalPing;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
	@Shadow
	private Minecraft minecraft;

	@Inject(method = "extractPingIcon", at = @At("HEAD"), cancellable = true)
	private void nifte$numericalPing(GuiGraphicsExtractor graphics, int slotWidth, int xo, int yo, PlayerInfo info, CallbackInfo ci) {
		if (!NifteConfig.get().numericalPing) {
			return;
		}

		String text = String.valueOf(Math.max(info.getLatency(), 0));
		int color = 0xFF000000 | NumericalPing.color(info.getLatency());
		int width = this.minecraft.font.width(text);
		graphics.text(this.minecraft.font, text, xo + slotWidth - width - 1, yo, color, true);
		ci.cancel();
	}
}
