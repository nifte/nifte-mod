package dev.nifte.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jspecify.annotations.Nullable;

import dev.nifte.feature.dropconfirm.DropConfirmFeature;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {
	@Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
	private void nifte$dropConfirm(@Nullable Slot slot, int slotId, int buttonNum, ContainerInput input, CallbackInfo ci) {
		CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
		if (DropConfirmFeature.handleSlotClick(screen.getMenu(), slot, slotId, input)) {
			ci.cancel();
		}
	}
}
