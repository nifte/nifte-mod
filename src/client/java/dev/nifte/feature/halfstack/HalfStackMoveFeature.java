package dev.nifte.feature.halfstack;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;
import dev.nifte.inventory.InventoryClicks;

public final class HalfStackMoveFeature {
	private HalfStackMoveFeature() {
	}

	public static boolean handleSlotClick(@Nullable Slot slot, int button, ContainerInput input) {
		if (slot == null || !canMoveHalf(slot, button, input)) {
			return false;
		}

		ItemStack template = slot.getItem().copy();
		InventoryClicks.pickupOne(slot.index);
		if (!tookHalf(slot, template)) {
			restoreCursor(slot);
			return !unchanged(slot, template);
		}

		// The right-click left half in the slot. Quick-move that half, then put the cursor stack back.
		InventoryClicks.quickMove(slot.index);
		restoreCursor(slot);
		return true;
	}

	private static boolean canMoveHalf(Slot slot, int button, ContainerInput input) {
		if (!NifteConfig.get().halfStackMoveEnabled || input != ContainerInput.QUICK_MOVE || button != InputConstants.MOUSE_BUTTON_RIGHT) {
			return false;
		}

		if (!slot.isActive() || slot.isFake() || !slot.hasItem() || !slot.mayPlace(slot.getItem())) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || !InventoryClicks.cursorEmpty() || !slot.mayPickup(minecraft.player)) {
			return false;
		}

		ItemStack stack = slot.getItem();
		return stack.getCount() > 1 && !(stack.getItem() instanceof BundleItem);
	}

	private static boolean tookHalf(Slot slot, ItemStack template) {
		int onCursor = (template.getCount() + 1) / 2;
		int inSlot = template.getCount() / 2;
		ItemStack carried = InventoryClicks.carried();
		return carried.getCount() == onCursor
			&& ItemStack.isSameItemSameComponents(carried, template)
			&& matches(slot.getItem(), template, inSlot);
	}

	private static boolean unchanged(Slot slot, ItemStack template) {
		return InventoryClicks.cursorEmpty() && matches(slot.getItem(), template, template.getCount());
	}

	private static boolean matches(ItemStack stack, ItemStack template, int count) {
		return stack.getCount() == count && ItemStack.isSameItemSameComponents(stack, template);
	}

	private static void restoreCursor(Slot slot) {
		if (InventoryClicks.cursorEmpty()) {
			return;
		}

		ItemStack carried = InventoryClicks.carried();
		boolean canReturn = slot.mayPlace(carried) && (slot.getItem().isEmpty() || ItemStack.isSameItemSameComponents(slot.getItem(), carried));
		if (canReturn) {
			InventoryClicks.pickup(slot.index);
		}

		if (!InventoryClicks.cursorEmpty()) {
			InventoryClicks.depositCarriedIntoPlayerInventory();
		}
	}
}
