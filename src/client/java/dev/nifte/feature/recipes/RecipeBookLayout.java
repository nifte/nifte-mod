package dev.nifte.feature.recipes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import dev.nifte.config.NifteConfig;
import dev.nifte.mixin.AbstractContainerScreenAccessor;

public final class RecipeBookLayout {
	public static final int BOOK_WIDTH = 147;

	private RecipeBookLayout() {
	}

	public static boolean keepCraftingCentered() {
		return NifteConfig.get().keepCraftingCentered;
	}

	public static int centeredLeftPos(int screenWidth, int imageWidth) {
		return (screenWidth - imageWidth) / 2;
	}

	public static int bookXOffset(int screenWidth, boolean widthTooNarrow) {
		if (!keepCraftingCentered() || widthTooNarrow) {
			return widthTooNarrow ? 0 : 86;
		}

		int inventoryLeft = inventoryLeftPos(screenWidth);
		return (screenWidth - BOOK_WIDTH) / 2 - (inventoryLeft - BOOK_WIDTH);
	}

	public static boolean treatBookAsBesideInventory(boolean widthTooNarrow) {
		return keepCraftingCentered() && !widthTooNarrow;
	}

	private static int inventoryLeftPos(int screenWidth) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.gui.screen() instanceof AbstractContainerScreen<?> screen) {
			return ((AbstractContainerScreenAccessor) screen).nifte$leftPos();
		}

		return centeredLeftPos(screenWidth, 176);
	}
}
