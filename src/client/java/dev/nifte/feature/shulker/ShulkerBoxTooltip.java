package dev.nifte.feature.shulker;

import java.util.List;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ShulkerBoxTooltip(List<ItemStack> items) implements TooltipComponent {
	public ShulkerBoxTooltip {
		items = List.copyOf(items);
	}
}
