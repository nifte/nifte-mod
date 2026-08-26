package dev.nifte.feature.sort;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import dev.nifte.config.NifteConfig;
import dev.nifte.inventory.InventoryClicks;
import dev.nifte.mixin.AbstractContainerScreenAccessor;

public final class ContainerSortFeature {
	private ContainerSortFeature() {
	}

	public static boolean handleClick(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
		if (!NifteConfig.get().containerSortEnabled || screen instanceof CreativeModeInventoryScreen) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options == null || !minecraft.options.keyPickItem.matchesMouse(event)) {
			return false;
		}

		Slot hovered = ((AbstractContainerScreenAccessor) screen).nifte$hoveredSlot();
		if (hovered == null || !hovered.mayPickup(minecraft.player)) {
			return false;
		}

		List<Slot> group = sortableGroup(screen.getMenu(), hovered, minecraft);
		if (group.size() < 2 || !InventoryClicks.cursorEmpty()) {
			return false;
		}

		compact(group, minecraft.player);
		sort(group);
		return true;
	}

	private static List<Slot> sortableGroup(AbstractContainerMenu menu, Slot hovered, Minecraft minecraft) {
		boolean playerInventory = InventoryClicks.isPlayerInventorySlot(hovered, minecraft);
		boolean hotbar = playerInventory && hovered.getContainerSlot() < 9;
		List<Slot> slots = new ArrayList<>();
		for (Slot slot : menu.slots) {
			if (!isSortable(slot)) {
				continue;
			}

			boolean slotPlayer = InventoryClicks.isPlayerInventorySlot(slot, minecraft);
			if (slotPlayer != playerInventory) {
				continue;
			}

			if (playerInventory) {
				boolean slotHotbar = slot.getContainerSlot() < 9;
				if (slotHotbar != hotbar || slot.getContainerSlot() >= 36) {
					continue;
				}
			}

			slots.add(slot);
		}

		return slots;
	}

	private static boolean isSortable(Slot slot) {
		return !(slot instanceof ResultSlot) && !(slot instanceof FurnaceResultSlot) && !slot.isFake() && slot.isActive();
	}

	private static void compact(List<Slot> slots, Player player) {
		for (int i = 0; i < slots.size(); i++) {
			Slot destination = slots.get(i);
			ItemStack destStack = destination.getItem();
			if (destStack.isEmpty() || destStack.getCount() >= destStack.getMaxStackSize()) {
				continue;
			}

			for (int j = i + 1; j < slots.size(); j++) {
				Slot source = slots.get(j);
				ItemStack sourceStack = source.getItem();
				if (sourceStack.isEmpty() || !ItemStack.isSameItemSameComponents(destStack, sourceStack) || !source.mayPickup(player)) {
					continue;
				}

				InventoryClicks.pickup(source.index);
				InventoryClicks.pickup(destination.index);
				if (!InventoryClicks.cursorEmpty()) {
					InventoryClicks.pickup(source.index);
				}

				destStack = destination.getItem();
				if (destStack.getCount() >= destStack.getMaxStackSize()) {
					break;
				}
			}
		}
	}

	private static void sort(List<Slot> slots) {
		for (int target = 0; target < slots.size(); target++) {
			int best = target;
			for (int candidate = target + 1; candidate < slots.size(); candidate++) {
				if (ItemStackSortOrder.compare(slots.get(candidate).getItem(), slots.get(best).getItem()) < 0) {
					best = candidate;
				}
			}

			if (best != target) {
				InventoryClicks.swapSlots(slots.get(target).index, slots.get(best).index);
			}
		}
	}
}
