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
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || cursorEmpty()) {
			return;
		}

		AbstractContainerMenu menu = minecraft.player.containerMenu;
		for (Slot slot : menu.slots) {
			if (!isPlayerMainInventorySlot(slot, minecraft)) {
				continue;
			}

			ItemStack inSlot = slot.getItem();
			ItemStack carried = carried();
			if (inSlot.isEmpty() || !ItemStack.isSameItemSameComponents(inSlot, carried) || inSlot.getCount() >= inSlot.getMaxStackSize()) {
				continue;
			}

			pickup(slot.index);
			if (cursorEmpty()) {
				return;
			}
		}

		for (Slot slot : menu.slots) {
			if (!isPlayerMainInventorySlot(slot, minecraft) || !slot.getItem().isEmpty()) {
				continue;
			}

			pickup(slot.index);
			if (cursorEmpty()) {
				return;
			}
		}
	}
}
