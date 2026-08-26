package dev.nifte.mixin;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import dev.nifte.feature.recipes.RecipeUnlock;

@Mixin(ClientRecipeBook.class)
public abstract class ClientRecipeBookMixin {
	@ModifyArg(
		method = "rebuildCollections",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/ClientRecipeBook;categorizeAndGroupRecipes(Ljava/lang/Iterable;)Ljava/util/Map;"
		)
	)
	private Iterable<RecipeDisplayEntry> nifte$includeUnlockedRecipes(Iterable<RecipeDisplayEntry> recipes) {
		return RecipeUnlock.recipesForBook(recipes);
	}
}
