package dev.nifte.feature.recipes;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import dev.nifte.config.NifteConfig;
import dev.nifte.inventory.InventoryClicks;
import dev.nifte.mixin.AbstractRecipeBookScreenAccessor;
import dev.nifte.mixin.RecipeBookComponentAccessor;
import dev.nifte.mixin.RecipeBookPageAccessor;

public final class RecipeFeatures {
	private static final int INSTANT_CRAFT_TIMEOUT_TICKS = 40;

	private static boolean instantCraftPending;
	private static boolean instantCraftAll;
	private static int instantCraftContainerId;
	private static int instantCraftResultSlot;
	private static int instantCraftWaitTicks;

	private RecipeFeatures() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(RecipeFeatures::tickInstantCraft);
	}

	public static boolean handlePageScroll(AbstractRecipeBookScreen<?> screen, double mouseX, double mouseY, double scrollY) {
		if (!NifteConfig.get().recipeBookScrollEnabled || scrollY == 0.0) {
			return false;
		}

		RecipeBookComponent<?> book = ((AbstractRecipeBookScreenAccessor) screen).nifte$recipeBookComponent();
		if (book == null || !book.isVisible()) {
			return false;
		}

		RecipeBookComponentAccessor accessor = (RecipeBookComponentAccessor) book;
		int xo = accessor.nifte$getXOrigin();
		int yo = accessor.nifte$getYOrigin();
		if (mouseX < xo || mouseY < yo || mouseX >= xo + 147 || mouseY >= yo + 166) {
			return false;
		}

		RecipeBookPageAccessor page = (RecipeBookPageAccessor) accessor.nifte$recipeBookPage();
		int current = page.nifte$currentPage();
		int total = page.nifte$totalPages();
		if (total <= 1) {
			return false;
		}

		int next = scrollY > 0.0 ? current - 1 : current + 1;
		if (next < 0 || next >= total) {
			return false;
		}

		page.nifte$setCurrentPage(next);
		page.nifte$updateButtonsForPage();
		return true;
	}

	public static void queueInstantCraft(AbstractContainerMenu menu, boolean recipeCraftable, boolean craftAll) {
		if (!NifteConfig.get().instantCraftEnabled || !recipeCraftable || !(menu instanceof AbstractCraftingMenu crafting)) {
			return;
		}

		instantCraftPending = true;
		instantCraftAll = craftAll;
		instantCraftContainerId = menu.containerId;
		instantCraftResultSlot = crafting.getResultSlot().index;
		instantCraftWaitTicks = 0;
	}

	private static void tickInstantCraft(Minecraft minecraft) {
		if (!instantCraftPending) {
			return;
		}

		if (!NifteConfig.get().instantCraftEnabled || minecraft.player == null || minecraft.gameMode == null) {
			clearInstantCraft();
			return;
		}

		AbstractContainerMenu menu = minecraft.player.containerMenu;
		if (menu.containerId != instantCraftContainerId || !(menu instanceof AbstractCraftingMenu crafting)) {
			clearInstantCraft();
			return;
		}

		Slot result = crafting.getResultSlot();
		if (result.index != instantCraftResultSlot || result.getItem().isEmpty() || !InventoryClicks.cursorEmpty()) {
			instantCraftWaitTicks++;
			if (instantCraftWaitTicks >= INSTANT_CRAFT_TIMEOUT_TICKS) {
				clearInstantCraft();
			}

			return;
		}

		if (instantCraftAll) {
			InventoryClicks.quickMove(result.index);
		} else {
			InventoryClicks.pickup(result.index);
			InventoryClicks.depositCarriedIntoPlayerInventory();
		}

		clearInstantCraft();
	}

	private static void clearInstantCraft() {
		instantCraftPending = false;
	}
}
