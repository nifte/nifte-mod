package dev.nifte.feature.food;

import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import dev.nifte.config.NifteConfig;

public final class FoodHungerName {
	private FoodHungerName() {
	}

	public static int nutrition(ItemStack stack) {
		if (!NifteConfig.get().foodHungerNameEnabled) {
			return 0;
		}

		FoodProperties food = stack.get(DataComponents.FOOD);
		if (food == null) {
			return 0;
		}

		return Math.max(0, food.nutrition());
	}

	public static Optional<TooltipComponent> image(ItemStack stack) {
		int nutrition = nutrition(stack);
		if (nutrition <= 0) {
			return Optional.empty();
		}

		return Optional.of(new FoodHungerTitle(nutrition));
	}
}
