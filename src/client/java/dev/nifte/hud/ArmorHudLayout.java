package dev.nifte.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

public final class ArmorHudLayout {
	static final int ROW_HEIGHT = 18;
	static final int ICON_SIZE = 16;
	static final int ICON_TEXT_GAP = 2;
	static final int TEXT_Y_OFFSET = 5;

	private static final int HOTBAR_HALF_WIDTH = 91;
	private static final int OFFHAND_SLOT_WIDTH = 29;
	private static final int HOTBAR_ITEM_Y_FROM_BOTTOM = 19;

	private ArmorHudLayout() {
	}

	static @Nullable ArmorHudPiece piece(LocalPlayer player, Font font, EquipmentSlot slot) {
		ItemStack stack = player.getItemBySlot(slot);
		if (stack.isEmpty() || !stack.isDamageableItem()) {
			return null;
		}

		int remaining = stack.getMaxDamage() - stack.getDamageValue();
		int percent = Mth.clamp(Math.round(remaining * 100.0F / stack.getMaxDamage()), 0, 100);
		String text = percent + "%";
		return new ArmorHudPiece(stack, text, durabilityColor(percent), font.width(text));
	}

	static int columnWidth(ArmorHudPiece[] pieces) {
		int maxText = 0;
		boolean any = false;
		for (ArmorHudPiece piece : pieces) {
			if (piece != null) {
				any = true;
				maxText = Math.max(maxText, piece.textWidth());
			}
		}

		if (!any) {
			return 0;
		}

		return ICON_SIZE + ICON_TEXT_GAP + maxText;
	}

	static boolean hasPiece(ArmorHudPiece[] pieces) {
		for (ArmorHudPiece piece : pieces) {
			if (piece != null) {
				return true;
			}
		}

		return false;
	}

	static int hotbarItemY(int guiHeight) {
		return guiHeight - HOTBAR_ITEM_Y_FROM_BOTTOM;
	}

	static int leftColumnX(LocalPlayer player, int guiWidth, int scaledWidth, int extraGap) {
		int rightEdge = guiWidth / 2 - HOTBAR_HALF_WIDTH - offhandWidth(player, true) - extraGap;
		return rightEdge - scaledWidth;
	}

	static int rightColumnX(LocalPlayer player, int guiWidth, int extraGap) {
		return guiWidth / 2 + HOTBAR_HALF_WIDTH + offhandWidth(player, false) + extraGap;
	}

	private static int offhandWidth(LocalPlayer player, boolean leftSide) {
		if (player.getOffhandItem().isEmpty()) {
			return 0;
		}

		boolean offhandOnLeft = player.getMainArm().getOpposite() == HumanoidArm.LEFT;
		return leftSide == offhandOnLeft ? OFFHAND_SLOT_WIDTH : 0;
	}

	static int durabilityColor(int percent) {
		if (percent > 50) {
			return 0x55FF55;
		}

		if (percent > 20) {
			return 0xFFFF55;
		}

		return 0xFF5555;
	}
}
