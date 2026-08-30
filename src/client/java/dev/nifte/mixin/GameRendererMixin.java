package dev.nifte.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.config.NifteConfigPreview;
import dev.nifte.config.NifteConfigScreen;
import dev.nifte.feature.autototem.AutoTotemFeature;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Inject(method = "extract", at = @At("HEAD"))
	private void nifte$previewSettings(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
		Screen screen = Minecraft.getInstance().gui.screen();
		if (NifteConfigScreen.isNifteScreen(screen)) {
			NifteConfigPreview.apply(screen);
		}
	}

	@Inject(method = "displayItemActivation", at = @At("TAIL"))
	private void nifte$autoTotem(ItemStack itemStack, CallbackInfo ci) {
		if (itemStack.is(Items.TOTEM_OF_UNDYING)) {
			AutoTotemFeature.onTotemPopped();
		}
	}
}
