package dev.nifte.feature.food;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public final class FoodHungerTooltips {
	private FoodHungerTooltips() {
	}

	public static List<ClientTooltipComponent> assemble(List<Component> texts, Optional<TooltipComponent> image) {
		Optional<FoodHungerTitle> hunger = image.filter(FoodHungerTitle.class::isInstance).map(FoodHungerTitle.class::cast);
		List<ClientTooltipComponent> components = new ArrayList<>();
		for (int i = 0; i < texts.size(); i++) {
			Component line = texts.get(i);
			if (i == 0 && hunger.isPresent()) {
				components.add(new ClientFoodHungerTooltip(line, hunger.get().nutrition()));
				continue;
			}

			components.add(ClientTooltipComponent.create(line.getVisualOrderText()));
		}

		image.filter(component -> !(component instanceof FoodHungerTitle))
			.ifPresent(component -> components.add(components.isEmpty() ? 0 : 1, ClientTooltipComponent.create(component)));
		if (texts.isEmpty() && hunger.isPresent()) {
			components.add(new ClientFoodHungerTooltip(Component.empty(), hunger.get().nutrition()));
		}

		return components;
	}
}
