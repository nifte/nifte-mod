package dev.nifte.feature.sort;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

import dev.nifte.config.NifteConfig;
import dev.nifte.input.NifteKeybinds;
import dev.nifte.inventory.InventoryClicks;
import dev.nifte.mixin.AbstractContainerScreenAccessor;

public final class ContainerSortFeature {
	private ContainerSortFeature() {
	}

	public static boolean handleClick(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
		return enabled(screen) && NifteKeybinds.sortContainer.matchesMouse(event) && sortHovered(screen);
	}

	public static boolean handleKey(AbstractContainerScreen<?> screen, KeyEvent event) {
		return enabled(screen)
			&& !(screen.getFocused() instanceof EditBox)
			&& NifteKeybinds.sortContainer.matches(event)
			&& sortHovered(screen);
	}

	private static boolean enabled(AbstractContainerScreen<?> screen) {
		return NifteConfig.get().containerSortEnabled
			&& NifteKeybinds.isBound(NifteKeybinds.sortContainer)
			&& !(screen instanceof CreativeModeInventoryScreen);
	}

	private static boolean sortHovered(AbstractContainerScreen<?> screen) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) {
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
		sort(group, minecraft.player);
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

	private static void sort(List<Slot> slots, Player player) {
		for (int target = 0; target < slots.size(); target++) {
			int best = target;
			for (int candidate = target + 1; candidate < slots.size(); candidate++) {
				if (ItemStackSortOrder.compare(slots.get(candidate).getItem(), slots.get(best).getItem()) < 0) {
					best = candidate;
				}
			}

			if (best != target) {
				exchange(slots.get(target), slots.get(best), player);
			}
		}
	}

	private static void exchange(Slot first, Slot second, Player player) {
		if (!bundleBlocksPickupSwap(first.getItem(), second.getItem())) {
			InventoryClicks.swapSlots(first.index, second.index);
			return;
		}

		// Left-clicking a bundle inserts the held stack. Swap through the hotbar instead.
		if (swapHotbarPair(first, second, player)) {
			return;
		}

		int scratch = findScratch(player, first, second);
		if (scratch < 0) {
			return;
		}

		InventoryClicks.swapWithHotbar(first.index, scratch);
		InventoryClicks.swapWithHotbar(second.index, scratch);
		InventoryClicks.swapWithHotbar(first.index, scratch);
	}

	private static boolean bundleBlocksPickupSwap(ItemStack first, ItemStack second) {
		if (first.isEmpty() || second.isEmpty()) {
			return false;
		}

		return first.getItem() instanceof BundleItem || second.getItem() instanceof BundleItem;
	}

	private static boolean swapHotbarPair(Slot first, Slot second, Player player) {
		int firstHotbar = hotbarIndex(first);
		if (firstHotbar >= 0 && first.mayPickup(player) && fitsIn(second, player.getInventory().getItem(firstHotbar), player)) {
			InventoryClicks.swapWithHotbar(second.index, firstHotbar);
			return true;
		}

		int secondHotbar = hotbarIndex(second);
		if (secondHotbar >= 0 && second.mayPickup(player) && fitsIn(first, player.getInventory().getItem(secondHotbar), player)) {
			InventoryClicks.swapWithHotbar(first.index, secondHotbar);
			return true;
		}

		return false;
	}

	private static int findScratch(Player player, Slot first, Slot second) {
		int skipFirst = hotbarIndex(first);
		int skipSecond = hotbarIndex(second);
		int occupied = -1;
		for (int index = 0; index < Inventory.SELECTION_SIZE; index++) {
			if (index == skipFirst || index == skipSecond) {
				continue;
			}

			ItemStack stack = player.getInventory().getItem(index);
			if (!canCycleThrough(first, second, stack, player)) {
				continue;
			}

			if (stack.isEmpty()) {
				return index;
			}

			if (occupied < 0) {
				occupied = index;
			}
		}

		ItemStack offhand = player.getInventory().getItem(Inventory.SLOT_OFFHAND);
		if (canCycleThrough(first, second, offhand, player)) {
			if (offhand.isEmpty() || occupied < 0) {
				return Inventory.SLOT_OFFHAND;
			}
		}

		return occupied;
	}

	private static boolean canCycleThrough(Slot first, Slot second, ItemStack scratch, Player player) {
		if (!first.mayPickup(player) || !second.mayPickup(player)) {
			return false;
		}

		if (!fitsIn(second, first.getItem(), player) || !fitsIn(first, second.getItem(), player)) {
			return false;
		}

		return scratch.isEmpty() || fitsIn(first, scratch, player);
	}

	private static boolean fitsIn(Slot slot, ItemStack stack, Player player) {
		return !stack.isEmpty()
			&& slot.mayPickup(player)
			&& slot.mayPlace(stack)
			&& stack.getCount() <= slot.getMaxStackSize(stack);
	}

	private static int hotbarIndex(Slot slot) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!InventoryClicks.isPlayerInventorySlot(slot, minecraft)) {
			return -1;
		}

		int index = slot.getContainerSlot();
		return Inventory.isHotbarSlot(index) ? index : -1;
	}
}
