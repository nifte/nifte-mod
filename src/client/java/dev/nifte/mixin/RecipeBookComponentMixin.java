package dev.nifte.mixin;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.inventory.RecipeBookMenu;

import dev.nifte.feature.recipes.RecipeBookLayout;
import dev.nifte.feature.recipes.RecipeFeatures;
import dev.nifte.feature.recipes.RecipeUnlock;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin<T extends RecipeBookMenu> {
	@Shadow
	@Final
	protected T menu;

	@Shadow
	private int width;

	@Shadow
	private boolean widthTooNarrow;

	@Shadow
	private int xOffset;

	@Shadow
	private ClientRecipeBook book;

	@Inject(method = "initVisuals", at = @At("HEAD"))
	private void nifte$refreshUnlockedRecipes(CallbackInfo ci) {
		RecipeUnlock.refreshCollectionsIfNeeded(this.book);
	}

	@Inject(
		method = "tryPlaceRecipe",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handlePlaceRecipe(ILnet/minecraft/world/item/crafting/display/RecipeDisplayId;Z)V",
			shift = At.Shift.AFTER
		)
	)
	private void nifte$instantCraft(RecipeCollection recipeCollection, RecipeDisplayId recipe, boolean useMaxItems, CallbackInfoReturnable<Boolean> cir) {
		RecipeFeatures.queueInstantCraft(this.menu, recipeCollection.isCraftable(recipe), useMaxItems);
	}

	@Inject(method = "updateScreenPosition", at = @At("HEAD"), cancellable = true)
	private void nifte$keepCraftingCentered(int screenWidth, int imageWidth, CallbackInfoReturnable<Integer> cir) {
		if (RecipeBookLayout.keepCraftingCentered()) {
			cir.setReturnValue(RecipeBookLayout.centeredLeftPos(screenWidth, imageWidth));
		}
	}

	@Inject(
		method = "initVisuals",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;getXOrigin()I",
			ordinal = 0
		)
	)
	private void nifte$alignBookToCenteredInventory(CallbackInfo ci) {
		this.xOffset = RecipeBookLayout.bookXOffset(this.width, this.widthTooNarrow);
	}

	@Inject(method = "isOffsetNextToMainGUI", at = @At("HEAD"), cancellable = true)
	private void nifte$bookStaysBesideInventory(CallbackInfoReturnable<Boolean> cir) {
		if (RecipeBookLayout.treatBookAsBesideInventory(this.widthTooNarrow)) {
			cir.setReturnValue(true);
		}
	}
}
