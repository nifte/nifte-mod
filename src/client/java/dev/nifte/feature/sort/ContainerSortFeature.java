package dev.nifte.feature.sort;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.tags.ItemTags;
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
			if (destStack.isEmpty() || isBundle(destStack) || destStack.getCount() >= destStack.getMaxStackSize()) {
				continue;
			}

			for (int j = i + 1; j < slots.size(); j++) {
				Slot source = slots.get(j);
				ItemStack sourceStack = source.getItem();
				if (sourceStack.isEmpty() || isBundle(sourceStack) || !ItemStack.isSameItemSameComponents(destStack, sourceStack) || !source.mayPickup(player)) {
					continue;
				}

				InventoryClicks.pickup(source.index);
				if (isBundle(InventoryClicks.carried()) || isBundle(destination.getItem())) {
					if (!InventoryClicks.cursorEmpty() && source.getItem().isEmpty()) {
						InventoryClicks.pickup(source.index);
					}
					continue;
				}

				InventoryClicks.pickup(destination.index);
				if (!InventoryClicks.cursorEmpty() && !isBundle(source.getItem())) {
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
				exchange(slots, slots.get(target), slots.get(best), player);
			}
		}
	}

	private static void exchange(List<Slot> slots, Slot first, Slot second, Player player) {
		if (!containsBundle(slots)) {
			InventoryClicks.swapSlots(first.index, second.index);
			return;
		}

		// A left click inserts the held stack into a bundle. Move bundles by placing onto empty slots.
		if (second.getItem().isEmpty()) {
			transfer(first, second);
			return;
		}

		if (first.getItem().isEmpty()) {
			transfer(second, first);
			return;
		}

		Slot parking = findParking(slots, first, second, player);
		if (parking != null) {
			moveThroughParking(first, second, parking);
		}
	}

	private static boolean containsBundle(List<Slot> slots) {
		for (Slot slot : slots) {
			if (isBundle(slot.getItem())) {
				return true;
			}
		}

		return false;
	}

	private static boolean isBundle(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		return stack.getItem() instanceof BundleItem
			|| stack.is(ItemTags.BUNDLES)
			|| stack.has(DataComponents.BUNDLE_CONTENTS);
	}

	private static Slot findParking(List<Slot> group, Slot first, Slot second, Player player) {
		ItemStack moving = first.getItem();
		if (!first.mayPickup(player) || !second.mayPickup(player) || !fitsIn(first, second.getItem(), player) || !fitsIn(second, moving, player)) {
			return null;
		}

		Slot inGroup = emptyParking(group, first, second, moving, player);
		if (inGroup != null) {
			return inGroup;
		}

		Slot playerSlot = emptyPlayerSlot(player, first, second, moving);
		if (playerSlot != null) {
			return playerSlot;
		}

		return emptyParking(player.containerMenu.slots, first, second, moving, player);
	}

	private static Slot emptyPlayerSlot(Player player, Slot first, Slot second, ItemStack moving) {
		Minecraft minecraft = Minecraft.getInstance();
		for (Slot slot : player.containerMenu.slots) {
			if (slot == first || slot == second || !slot.getItem().isEmpty() || !slot.mayPickup(player) || !fitsIn(slot, moving, player)) {
				continue;
			}

			boolean offhand = InventoryClicks.isPlayerInventorySlot(slot, minecraft) && slot.getContainerSlot() == Inventory.SLOT_OFFHAND;
			if (InventoryClicks.isPlayerMainInventorySlot(slot, minecraft) || offhand) {
				return slot;
			}
		}

		return null;
	}

	private static Slot emptyParking(List<Slot> slots, Slot first, Slot second, ItemStack moving, Player player) {
		for (Slot slot : slots) {
			if (slot == first || slot == second || !isSortable(slot) || !slot.getItem().isEmpty() || !slot.mayPickup(player)) {
				continue;
			}

			if (fitsIn(slot, moving, player)) {
				return slot;
			}
		}

		return null;
	}

	private static void moveThroughParking(Slot first, Slot second, Slot parking) {
		if (!transfer(first, parking)) {
			return;
		}

		if (!transfer(second, first)) {
			transfer(parking, first);
			return;
		}

		if (!transfer(parking, second)) {
			transfer(first, second);
			transfer(parking, first);
		}
	}

	private static boolean transfer(Slot from, Slot to) {
		if (!take(from)) {
			return false;
		}

		if (placeOntoEmpty(to)) {
			return true;
		}

		placeOntoEmpty(from);
		return false;
	}

	private static boolean take(Slot slot) {
		if (!InventoryClicks.cursorEmpty() || slot.getItem().isEmpty()) {
			return false;
		}

		InventoryClicks.pickup(slot.index);
		return !InventoryClicks.cursorEmpty() && slot.getItem().isEmpty();
	}

	private static boolean placeOntoEmpty(Slot slot) {
		if (InventoryClicks.cursorEmpty() || !slot.getItem().isEmpty()) {
			return false;
		}

		ItemStack carried = InventoryClicks.carried().copy();
		InventoryClicks.pickup(slot.index);
		ItemStack placed = slot.getItem();
		return InventoryClicks.cursorEmpty()
			&& placed.getCount() == carried.getCount()
			&& ItemStack.isSameItemSameComponents(placed, carried);
	}

	private static boolean fitsIn(Slot slot, ItemStack stack, Player player) {
		return !stack.isEmpty()
			&& slot.mayPickup(player)
			&& slot.mayPlace(stack)
			&& stack.getCount() <= slot.getMaxStackSize(stack);
	}
}
