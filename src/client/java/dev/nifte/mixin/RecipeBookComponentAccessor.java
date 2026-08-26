package dev.nifte.mixin;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentAccessor {
	@Invoker("getXOrigin")
	int nifte$getXOrigin();

	@Invoker("getYOrigin")
	int nifte$getYOrigin();

	@Accessor("recipeBookPage")
	RecipeBookPage nifte$recipeBookPage();
}
