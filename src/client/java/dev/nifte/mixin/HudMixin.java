package dev.nifte.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.food.FoodHungerHud;

@Mixin(Hud.class)
public abstract class HudMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private ItemStack lastToolHighlight;

	@Shadow
	private int toolHighlightTimer;

	@Shadow
	public abstract Font getFont();

	@Inject(method = "extractSelectedItemName", at = @At("HEAD"), cancellable = true)
	private void nifte$foodHungerName(GuiGraphicsExtractor graphics, CallbackInfo ci) {
		if (FoodHungerHud.extract(graphics, this.minecraft, this.getFont(), this.lastToolHighlight, this.toolHighlightTimer)) {
			ci.cancel();
		}
	}
}
