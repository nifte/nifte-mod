package dev.nifte.mixin;

import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.config.ConfigKeybindEditor;
import dev.nifte.config.NifteConfigScreen;

@Mixin(value = AbstractConfigScreen.class, remap = false)
public abstract class AbstractConfigScreenMixin {
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, remap = true)
	private void nifte$keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!NifteConfigScreen.isNifteScreen((Screen) (Object) this)) {
			return;
		}

		if (ConfigKeybindEditor.keyPressed(event)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = true)
	private void nifte$mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		if (!NifteConfigScreen.isNifteScreen((Screen) (Object) this)) {
			return;
		}

		if (ConfigKeybindEditor.mouseClicked(event)) {
			cir.setReturnValue(true);
		}
	}
}
