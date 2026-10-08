package dev.nifte.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class InventoryClicks {
	private InventoryClicks() {
	}

	public static void click(int slotId, int button, ContainerInput input) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.gameMode == null) {
			return;
		}

		AbstractContainerMenu menu = minecraft.player.containerMenu;
		minecraft.gameMode.handleContainerInput(menu.containerId, slotId, button, input, minecraft.player);
	}

	public static void pickup(int slotId) {
		click(slotId, 0, ContainerInput.PICKUP);
	}

	public static void pickupOne(int slotId) {
		click(slotId, 1, ContainerInput.PICKUP);
	}

	public static void quickMove(int slotId) {
		click(slotId, 0, ContainerInput.QUICK_MOVE);
	}

	public static void swapOffhand(int slotId) {
		click(slotId, 40, ContainerInput.SWAP);
	}

	public static boolean cursorEmpty() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.player != null && minecraft.player.containerMenu.getCarried().isEmpty();
	}

	public static ItemStack carried() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.player == null ? ItemStack.EMPTY : minecraft.player.containerMenu.getCarried();
	}

	public static void swapSlots(int first, int second) {
		if (first == second) {
			return;
		}

		pickup(first);
		pickup(second);
		pickup(first);
	}

	public static void swapWithHotbar(int slotId, int hotbarSlot) {
		click(slotId, hotbarSlot, ContainerInput.SWAP);
	}

	public static int playerMenuSlot(Minecraft minecraft, int containerSlot) {
		if (minecraft.player == null) {
			return -1;
		}

		for (Slot slot : minecraft.player.inventoryMenu.slots) {
			if (isPlayerInventorySlot(slot, minecraft) && slot.getContainerSlot() == containerSlot) {
				return slot.index;
			}
		}

		return -1;
	}

	public static boolean isPlayerInventorySlot(Slot slot, Minecraft minecraft) {
		return minecraft.player != null && slot.container == minecraft.player.getInventory();
	}

	public static boolean isPlayerMainInventorySlot(Slot slot, Minecraft minecraft) {
		if (!isPlayerInventorySlot(slot, minecraft)) {
			return false;
		}

		int containerSlot = slot.getContainerSlot();
		return containerSlot >= 0 && containerSlot < Inventory.INVENTORY_SIZE;
	}

	public static void depositCarriedIntoPlayerInventory() {
		depositCarried(false);
	}

	public static void depositCarriedPreferringEmptyHotbar() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || cursorEmpty()) {
			return;
		}

		if (hasEmptyHotbarSlot(minecraft.player.containerMenu, minecraft)) {
			depositCarried(true);
			return;
		}

		depositCarried(false);
	}

	private static void depositCarried(boolean hotbarFirst) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || cursorEmpty()) {
			return;
		}

		AbstractContainerMenu menu = minecraft.player.containerMenu;
		if (hotbarFirst) {
			transferCarried(menu, minecraft, true, false);
			if (cursorEmpty()) {
				return;
			}

			transferCarried(menu, minecraft, true, true);
			if (cursorEmpty()) {
				return;
			}
		}

		transferCarried(menu, minecraft, false, false);
		if (cursorEmpty()) {
			return;
		}

		transferCarried(menu, minecraft, false, true);
	}

	private static void transferCarried(AbstractContainerMenu menu, Minecraft minecraft, boolean hotbarOnly, boolean emptySlots) {
		for (Slot slot : menu.slots) {
			if (!isDepositSlot(slot, minecraft, hotbarOnly) || !acceptsCarried(slot, emptySlots)) {
				continue;
			}

			pickup(slot.index);
			if (cursorEmpty()) {
				return;
			}
		}
	}

	private static boolean acceptsCarried(Slot slot, boolean emptySlots) {
		ItemStack inSlot = slot.getItem();
		if (emptySlots) {
			return inSlot.isEmpty();
		}

		ItemStack carried = carried();
		return !inSlot.isEmpty()
			&& ItemStack.isSameItemSameComponents(inSlot, carried)
			&& inSlot.getCount() < inSlot.getMaxStackSize();
	}

	private static boolean isDepositSlot(Slot slot, Minecraft minecraft, boolean hotbarOnly) {
		if (!isPlayerMainInventorySlot(slot, minecraft)) {
			return false;
		}

		return !hotbarOnly || Inventory.isHotbarSlot(slot.getContainerSlot());
	}

	private static boolean hasEmptyHotbarSlot(AbstractContainerMenu menu, Minecraft minecraft) {
		for (Slot slot : menu.slots) {
			if (isDepositSlot(slot, minecraft, true) && slot.getItem().isEmpty()) {
				return true;
			}
		}

		return false;
	}
}
