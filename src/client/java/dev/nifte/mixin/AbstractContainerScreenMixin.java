package dev.nifte.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.drag.ContainerDragFeature;
import dev.nifte.feature.dropconfirm.DropConfirmFeature;
import dev.nifte.feature.itemscroll.ItemScrollFeature;
import dev.nifte.feature.recipes.RecipeFeatures;
import dev.nifte.feature.sort.ContainerSortFeature;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {
	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void nifte$mouseScrolled(double x, double y, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		if (screen instanceof AbstractRecipeBookScreen<?> recipeScreen && RecipeFeatures.handlePageScroll(recipeScreen, x, y, scrollY)) {
			cir.setReturnValue(true);
			return;
		}

		if (ItemScrollFeature.handleScroll(screen, scrollY)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void nifte$mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		if (ContainerSortFeature.handleClick(screen, event)) {
			cir.setReturnValue(true);
			return;
		}

		if (ContainerDragFeature.handleClick(screen, event)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void nifte$keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		if (ContainerSortFeature.handleKey(screen, event)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
	private void nifte$mouseDragged(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
		if (ContainerDragFeature.handleDrag((AbstractContainerScreen<?>) (Object) this, event)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	private void nifte$mouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (ContainerDragFeature.handleRelease((AbstractContainerScreen<?>) (Object) this, event)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
	private void nifte$dropConfirm(Slot slot, int slotId, int buttonNum, ContainerInput input, CallbackInfo ci) {
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		if (DropConfirmFeature.handleSlotClick(screen.getMenu(), slot, slotId, input)) {
			ci.cancel();
		}
	}
}
