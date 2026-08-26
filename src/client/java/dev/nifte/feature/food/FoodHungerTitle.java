package dev.nifte.feature.food;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record FoodHungerTitle(int nutrition) implements TooltipComponent {
}
