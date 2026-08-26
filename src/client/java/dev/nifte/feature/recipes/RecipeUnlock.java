package dev.nifte.feature.recipes;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

import dev.nifte.config.NifteConfig;

public final class RecipeUnlock {
	private static Boolean appliedUnlock;

	private RecipeUnlock() {
	}

	public static Iterable<RecipeDisplayEntry> recipesForBook(Iterable<RecipeDisplayEntry> known) {
		if (!NifteConfig.get().unlockAllRecipes) {
			return known;
		}

		Minecraft minecraft = Minecraft.getInstance();
		IntegratedServer server = minecraft.getSingleplayerServer();
		if (server == null) {
			return known;
		}

		Map<RecipeDisplayId, RecipeDisplayEntry> recipes = new HashMap<>();
		for (RecipeDisplayEntry entry : known) {
			recipes.put(entry.id(), entry);
		}

		RecipeManager manager = server.getRecipeManager();
		for (RecipeHolder<?> holder : manager.getRecipes()) {
			if (holder.value().isSpecial()) {
				continue;
			}

			manager.listDisplaysForRecipe(holder.id(), entry -> recipes.putIfAbsent(entry.id(), entry));
		}

		return recipes.values();
	}

	public static void refreshCollectionsIfNeeded(ClientRecipeBook book) {
		if (book == null) {
			return;
		}

		boolean unlock = NifteConfig.get().unlockAllRecipes;
		if (appliedUnlock != null && appliedUnlock == unlock) {
			return;
		}

		appliedUnlock = unlock;
		book.rebuildCollections();

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.getConnection() != null && minecraft.level != null) {
			minecraft.getConnection().searchTrees().updateRecipes(book, minecraft.level);
		}
	}
}
