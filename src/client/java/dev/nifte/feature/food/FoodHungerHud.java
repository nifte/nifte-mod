package dev.nifte.feature.food;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

public final class FoodHungerHud {
	private FoodHungerHud() {
	}

	public static boolean extract(GuiGraphicsExtractor graphics, Minecraft minecraft, Font font, ItemStack stack, int toolHighlightTimer) {
		int nutrition = FoodHungerName.nutrition(stack);
		if (nutrition <= 0) {
			return false;
		}

		MutableComponent name = Component.empty().append(stack.getHoverName()).withStyle(stack.getRarity().color());
		if (stack.has(DataComponents.CUSTOM_NAME)) {
			name.withStyle(ChatFormatting.ITALIC);
		}

		int nameWidth = font.width(name);
		int totalWidth = nameWidth + FoodHungerIcons.NAME_GAP + FoodHungerIcons.width(nutrition);
		int x = (graphics.guiWidth() - totalWidth) / 2;
		int y = graphics.guiHeight() - 59;
		if (!minecraft.gameMode.canHurtPlayer()) {
			y += 14;
		}

		int alpha = (int) (toolHighlightTimer * 256.0F / 10.0F);
		if (alpha > 255) {
			alpha = 255;
		}

		if (alpha > 0) {
			int color = ARGB.white(alpha);
			graphics.textWithBackdrop(font, name, x, y, totalWidth, color);
			FoodHungerIcons.draw(graphics, x + nameWidth + FoodHungerIcons.NAME_GAP, y, nutrition, color);
		}

		return true;
	}
}
