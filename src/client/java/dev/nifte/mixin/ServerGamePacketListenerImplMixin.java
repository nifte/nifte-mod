package dev.nifte.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.nifte.feature.recipes.RecipeUnlock;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Redirect(
		method = "handlePlaceRecipe",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/stats/ServerRecipeBook;contains(Lnet/minecraft/resources/ResourceKey;)Z"
		)
	)
	private boolean nifte$placeUnbookedRecipes(ServerRecipeBook book, ResourceKey<Recipe<?>> id) {
		return RecipeUnlock.mayPlace(book.contains(id));
	}
}
