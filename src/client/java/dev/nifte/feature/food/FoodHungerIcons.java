package dev.nifte.feature.food;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

final class FoodHungerIcons {
	static final int NAME_GAP = 4;
	private static final int SIZE = 9;
	private static final int STEP = 8;
	private static final Identifier EMPTY = Identifier.withDefaultNamespace("hud/food_empty");
	private static final Identifier FULL = Identifier.withDefaultNamespace("hud/food_full");
	private static final Identifier HALF = Identifier.withDefaultNamespace("hud/food_half");

	private FoodHungerIcons() {
	}

	static int width(int nutrition) {
		int shanks = shankCount(nutrition);
		if (shanks <= 0) {
			return 0;
		}

		return (shanks - 1) * STEP + SIZE;
	}

	static void draw(GuiGraphicsExtractor graphics, int x, int y, int nutrition, int color) {
		int remaining = nutrition;
		int index = 0;
		while (remaining >= 2) {
			shank(graphics, x + index * STEP, y, FULL, color);
			remaining -= 2;
			index++;
		}

		if (remaining == 1) {
			shank(graphics, x + index * STEP, y, HALF, color);
		}
	}

	private static int shankCount(int nutrition) {
		return (nutrition + 1) / 2;
	}

	private static void shank(GuiGraphicsExtractor graphics, int x, int y, Identifier fill, int color) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY, x, y, SIZE, SIZE, color);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, fill, x, y, SIZE, SIZE, color);
	}
}
