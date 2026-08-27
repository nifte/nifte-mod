package dev.nifte.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;

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
		List<ArmorHudPiece> pieces = cornerPieces(minecraft.player, minecraft.font, config);
		if (pieces.isEmpty()) {
			return;
		}

		int maxWidth = 0;
		for (ArmorHudPiece piece : pieces) {
			maxWidth = Math.max(maxWidth, ArmorHudLayout.ICON_SIZE + ArmorHudLayout.ICON_TEXT_GAP + piece.textWidth());
		}

		int contentHeight = pieces.size() * ArmorHudLayout.ROW_HEIGHT;
		int x = HudLayout.x(config.armorAnchor, graphics.guiWidth(), config.armorOffsetX, (int) (maxWidth * config.armorScale));
		int y = HudLayout.y(config.armorAnchor, graphics.guiHeight(), config.armorOffsetY, (int) (contentHeight * config.armorScale));

		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(config.armorScale, config.armorScale);

		for (int i = 0; i < pieces.size(); i++) {
			ArmorHudPiece piece = pieces.get(i);
			int rowY = i * ArmorHudLayout.ROW_HEIGHT;
			graphics.item(piece.stack(), 0, rowY);
			graphics.text(minecraft.font, piece.text(), ArmorHudLayout.ICON_SIZE + ArmorHudLayout.ICON_TEXT_GAP, rowY + ArmorHudLayout.TEXT_Y_OFFSET, 0xFF000000 | piece.color(), true);
		}

		graphics.pose().popMatrix();
	}

	private static List<ArmorHudPiece> cornerPieces(LocalPlayer player, Font font, NifteConfig config) {
		List<ArmorHudPiece> pieces = new ArrayList<>(6);
		for (EquipmentSlot slot : SLOTS) {
			ArmorHudPiece piece = ArmorHudLayout.piece(player, font, slot);
			if (piece != null) {
				pieces.add(piece);
			}
		}

		if (config.armorShowHeldItems) {
			ArmorHudPiece mainHand = ArmorHudLayout.piece(font, player.getMainHandItem());
			if (mainHand != null) {
				pieces.add(mainHand);
			}

			ArmorHudPiece offHand = ArmorHudLayout.piece(font, player.getOffhandItem());
			if (offHand != null) {
				pieces.add(offHand);
			}
		}

		return pieces;
	}

	private static void extractHotbar(GuiGraphicsExtractor graphics, NifteConfig config, Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		Font font = minecraft.font;
		ArmorHudPiece[] left = pieces(player, font, LEFT_SLOTS);
		ArmorHudPiece[] right = pieces(player, font, RIGHT_SLOTS);
		ArmorHudPiece leftHeld = config.armorShowHeldItems ? ArmorHudLayout.heldOnSide(player, font, true) : null;
		ArmorHudPiece rightHeld = config.armorShowHeldItems ? ArmorHudLayout.heldOnSide(player, font, false) : null;
		boolean anyArmor = ArmorHudLayout.hasPiece(left) || ArmorHudLayout.hasPiece(right);
		boolean anyHeld = leftHeld != null || rightHeld != null;
		if (!anyArmor && !anyHeld) {
			return;
		}

		if (anyHeld && anyArmor) {
			left = ArmorHudLayout.withHeldRow(leftHeld, left);
			right = ArmorHudLayout.withHeldRow(rightHeld, right);
		} else if (anyHeld) {
			left = new ArmorHudPiece[] { leftHeld };
			right = new ArmorHudPiece[] { rightHeld };
		}

		int topY = ArmorHudLayout.hotbarTopY(graphics.guiHeight(), Math.max(left.length, right.length), config.armorScale);
		drawHotbarColumn(graphics, font, left, true, ArmorHudLayout.leftColumnX(
			player,
			graphics.guiWidth(),
			Math.round(ArmorHudLayout.columnWidth(left) * config.armorScale),
			config.armorOffsetX
		), topY, config.armorScale);
		drawHotbarColumn(
			graphics,
			font,
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
