package dev.nifte.mixin;

import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookPage.class)
public interface RecipeBookPageAccessor {
	@Accessor("currentPage")
	int nifte$currentPage();

	@Accessor("currentPage")
	void nifte$setCurrentPage(int page);

	@Accessor("totalPages")
	int nifte$totalPages();

	@Invoker("updateButtonsForPage")
	void nifte$updateButtonsForPage();
}
