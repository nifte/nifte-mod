package dev.nifte.feature.food;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public final class ClientFoodHungerTooltip implements ClientTooltipComponent {
	private final Component name;
	private final int nutrition;

	public ClientFoodHungerTooltip(Component name, int nutrition) {
		this.name = name;
		this.nutrition = nutrition;
	}

	@Override
	public int getWidth(Font font) {
		return font.width(this.name) + FoodHungerIcons.NAME_GAP + FoodHungerIcons.width(this.nutrition);
	}

	@Override
	public int getHeight(Font font) {
		return 10;
	}

	@Override
	public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
		graphics.text(font, this.name, x, y, -1, true);
	}

	@Override
	public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
		FoodHungerIcons.draw(graphics, x + font.width(this.name) + FoodHungerIcons.NAME_GAP, y, this.nutrition, -1);
	}
}
