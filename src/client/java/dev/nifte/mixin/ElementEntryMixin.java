package dev.nifte.mixin;

import me.shedaniel.clothconfig2.gui.widget.DynamicElementListWidget;

import net.minecraft.client.gui.components.events.GuiEventListener;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DynamicElementListWidget.ElementEntry.class, remap = false)
public abstract class ElementEntryMixin {
	@Shadow
	private GuiEventListener focused;

	@Inject(method = "setFocused", at = @At("HEAD"), cancellable = true, remap = true)
	private void nifte$keepNestedFocus(GuiEventListener listener, CallbackInfo ci) {
		if (this.focused == listener) {
			ci.cancel();
		}
	}
}
