package dev.nifte.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import dev.nifte.config.ArmorHudAnchor;
import dev.nifte.config.NifteConfig;

public final class ArmorHud {
	private static final EquipmentSlot[] SLOTS = {
		EquipmentSlot.HEAD,
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET
	};
	private static final EquipmentSlot[] LEFT_SLOTS = {
		EquipmentSlot.HEAD,
		EquipmentSlot.CHEST
	};
	private static final EquipmentSlot[] RIGHT_SLOTS = {
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET
	};

	private ArmorHud() {
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		NifteConfig config = NifteConfig.get();
		Minecraft minecraft = Minecraft.getInstance();
		if (!config.armorHudEnabled || minecraft.player == null) {
			return;
		}

		if (config.armorAnchor == ArmorHudAnchor.HOTBAR) {
			extractHotbar(graphics, config, minecraft);
			return;
		}

		extractCorner(graphics, config, minecraft);
	}

	private static void extractCorner(GuiGraphicsExtractor graphics, NifteConfig config, Minecraft minecraft) {
		int line = 0;
		int maxWidth = 0;
		String[] lines = new String[SLOTS.length];
		ItemStack[] stacks = new ItemStack[SLOTS.length];
		int[] colors = new int[SLOTS.length];

		for (int i = 0; i < SLOTS.length; i++) {
			ArmorHudPiece piece = ArmorHudLayout.piece(minecraft.player, minecraft.font, SLOTS[i]);
			if (piece == null) {
				continue;
			}

			lines[line] = piece.text();
			stacks[line] = piece.stack();
			colors[line] = piece.color();
			maxWidth = Math.max(maxWidth, ArmorHudLayout.ICON_SIZE + ArmorHudLayout.ICON_TEXT_GAP + piece.textWidth());
			line++;
		}

		if (line == 0) {
			return;
		}

		int contentHeight = line * ArmorHudLayout.ROW_HEIGHT;
		int x = HudLayout.x(config.armorAnchor, graphics.guiWidth(), config.armorOffsetX, (int) (maxWidth * config.armorScale));
		int y = HudLayout.y(config.armorAnchor, graphics.guiHeight(), config.armorOffsetY, (int) (contentHeight * config.armorScale));

		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(config.armorScale, config.armorScale);

		for (int i = 0; i < line; i++) {
			int rowY = i * ArmorHudLayout.ROW_HEIGHT;
			graphics.item(stacks[i], 0, rowY);
			graphics.text(minecraft.font, lines[i], ArmorHudLayout.ICON_SIZE + ArmorHudLayout.ICON_TEXT_GAP, rowY + ArmorHudLayout.TEXT_Y_OFFSET, 0xFF000000 | colors[i], true);
		}

		graphics.pose().popMatrix();
	}

	private static void extractHotbar(GuiGraphicsExtractor graphics, NifteConfig config, Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		ArmorHudPiece[] left = pieces(player, minecraft.font, LEFT_SLOTS);
		ArmorHudPiece[] right = pieces(player, minecraft.font, RIGHT_SLOTS);
		if (!ArmorHudLayout.hasPiece(left) && !ArmorHudLayout.hasPiece(right)) {
			return;
		}

		int topY = ArmorHudLayout.hotbarItemY(graphics.guiHeight()) - Math.round(ArmorHudLayout.ROW_HEIGHT * config.armorScale);
		drawHotbarColumn(graphics, minecraft.font, left, true, ArmorHudLayout.leftColumnX(
			player,
			graphics.guiWidth(),
			Math.round(ArmorHudLayout.columnWidth(left) * config.armorScale),
			config.armorOffsetX
		), topY, config.armorScale);
		drawHotbarColumn(
			graphics,
			minecraft.font,
			right,
			false,
			ArmorHudLayout.rightColumnX(player, graphics.guiWidth(), config.armorOffsetX),
			topY,
			config.armorScale
		);
	}

	private static ArmorHudPiece[] pieces(LocalPlayer player, Font font, EquipmentSlot[] slots) {
		ArmorHudPiece[] pieces = new ArmorHudPiece[slots.length];
		for (int i = 0; i < slots.length; i++) {
			pieces[i] = ArmorHudLayout.piece(player, font, slots[i]);
		}

		return pieces;
	}

	private static void drawHotbarColumn(
		GuiGraphicsExtractor graphics,
		Font font,
		ArmorHudPiece[] pieces,
		boolean iconTowardHotbar,
		int x,
		int y,
		float scale
	) {
		if (!ArmorHudLayout.hasPiece(pieces)) {
			return;
		}

		int columnWidth = ArmorHudLayout.columnWidth(pieces);
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(scale, scale);
		for (int i = 0; i < pieces.length; i++) {
			ArmorHudPiece piece = pieces[i];
			if (piece == null) {
				continue;
			}

			int rowY = i * ArmorHudLayout.ROW_HEIGHT;
			int iconX = iconTowardHotbar ? columnWidth - ArmorHudLayout.ICON_SIZE : 0;
			int textX = iconTowardHotbar
				? iconX - ArmorHudLayout.ICON_TEXT_GAP - piece.textWidth()
				: ArmorHudLayout.ICON_SIZE + ArmorHudLayout.ICON_TEXT_GAP;
			graphics.item(piece.stack(), iconX, rowY);
			graphics.text(font, piece.text(), textX, rowY + ArmorHudLayout.TEXT_Y_OFFSET, 0xFF000000 | piece.color(), true);
		}

		graphics.pose().popMatrix();
	}
}
