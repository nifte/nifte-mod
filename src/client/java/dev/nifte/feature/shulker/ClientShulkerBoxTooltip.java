package dev.nifte.feature.shulker;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public final class ClientShulkerBoxTooltip implements ClientTooltipComponent {
	private static final Identifier SLOT_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_background");
	private static final int SLOT_SIZE = 18;
	private static final int BACKGROUND_SIZE = 22;
	private static final int ITEM_PADDING = 1;
	private static final int COLUMNS = 9;
	private static final int ROWS = 3;
	private static final int GRID_WIDTH = COLUMNS * SLOT_SIZE;
	private final ShulkerBoxTooltip tooltip;

	public ClientShulkerBoxTooltip(ShulkerBoxTooltip tooltip) {
		this.tooltip = tooltip;
	}

	@Override
	public int getHeight(Font font) {
		return ROWS * SLOT_SIZE;
	}

	@Override
	public int getWidth(Font font) {
		return GRID_WIDTH;
	}

	@Override
	public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
		int gridX = x + (w - GRID_WIDTH) / 2;
		for (int slot = 0; slot < ShulkerBoxBlockEntity.CONTAINER_SIZE; slot++) {
			int col = slot % COLUMNS;
			int row = slot / COLUMNS;
			extractSlot(font, graphics, itemAt(slot), gridX + col * SLOT_SIZE, y + row * SLOT_SIZE, slot);
		}
	}

	private ItemStack itemAt(int slot) {
		if (slot < 0 || slot >= this.tooltip.items().size()) {
			return ItemStack.EMPTY;
		}

		return this.tooltip.items().get(slot);
	}

	private static void extractSlot(Font font, GuiGraphicsExtractor graphics, ItemStack item, int x, int y, int seed) {
		int backgroundOffset = (SLOT_SIZE - BACKGROUND_SIZE) / 2;
		graphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			SLOT_BACKGROUND_SPRITE,
			x + backgroundOffset,
			y + backgroundOffset,
			BACKGROUND_SIZE,
			BACKGROUND_SIZE
		);
		if (!item.isEmpty()) {
			graphics.item(item, x + ITEM_PADDING, y + ITEM_PADDING, seed);
			graphics.itemDecorations(font, item, x + ITEM_PADDING, y + ITEM_PADDING);
		}
	}
}
