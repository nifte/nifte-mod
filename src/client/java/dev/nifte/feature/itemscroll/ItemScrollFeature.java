package dev.nifte.feature.itemscroll;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import dev.nifte.config.NifteConfig;
import dev.nifte.inventory.InventoryClicks;
import dev.nifte.mixin.AbstractContainerScreenAccessor;

public final class ItemScrollFeature {
	private ItemScrollFeature() {
	}

	public static boolean handleScroll(AbstractContainerScreen<?> screen, double scrollY) {
		if (!NifteConfig.get().itemScrollEnabled || scrollY == 0.0 || screen instanceof CreativeModeInventoryScreen) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player == null || !InventoryClicks.cursorEmpty()) {
			return false;
		}

		Slot hovered = ((AbstractContainerScreenAccessor) screen).nifte$hoveredSlot();
		if (hovered == null || !hovered.hasItem() || hovered instanceof ResultSlot || hovered.isFake()) {
			return false;
		}

		boolean toPlayer = scrollY < 0.0;
		boolean hoveredIsPlayer = InventoryClicks.isPlayerInventorySlot(hovered, minecraft);
		if (isTakeOnlySlot(hovered)) {
			if (!toPlayer || hoveredIsPlayer || !hovered.mayPickup(player)) {
				return false;
			}

			InventoryClicks.quickMove(hovered.index);
			return true;
		}

		if (toPlayer == hoveredIsPlayer) {
			return moveOneFromOtherInventory(screen.getMenu(), hovered, hoveredIsPlayer, player);
		}

		return moveOneFromHovered(screen.getMenu(), hovered, hoveredIsPlayer, player);
	}

	private static boolean moveOneFromHovered(AbstractContainerMenu menu, Slot source, boolean sourceIsPlayer, Player player) {
		if (!source.mayPickup(player)) {
			return false;
		}

		Slot destination = findDestination(menu, source.getItem(), source, sourceIsPlayer);
		if (destination == null) {
			return false;
		}

		InventoryClicks.pickup(source.index);
		InventoryClicks.pickupOne(destination.index);
		returnRemainingToSource(source);
		return true;
	}

	private static boolean moveOneFromOtherInventory(AbstractContainerMenu menu, Slot hovered, boolean hoveredIsPlayer, Player player) {
		ItemStack target = hovered.getItem();
		Slot source = null;
		for (Slot slot : menu.slots) {
			if (slot == hovered || slot instanceof ResultSlot || slot.isFake() || !slot.hasItem() || isTakeOnlySlot(slot)) {
				continue;
			}

			boolean playerSlot = InventoryClicks.isPlayerInventorySlot(slot, Minecraft.getInstance());
			if (playerSlot == hoveredIsPlayer) {
				continue;
			}

			if (ItemStack.isSameItemSameComponents(slot.getItem(), target) && slot.mayPickup(player)) {
				source = slot;
				break;
			}
		}

		if (source == null) {
			return false;
		}

		InventoryClicks.pickup(source.index);
		InventoryClicks.pickupOne(hovered.index);
		returnRemainingToSource(source);
		return true;
	}

	private static void returnRemainingToSource(Slot source) {
		if (InventoryClicks.cursorEmpty()) {
			return;
		}

		if (source.mayPlace(InventoryClicks.carried())) {
			InventoryClicks.pickup(source.index);
		}

		if (!InventoryClicks.cursorEmpty()) {
			InventoryClicks.depositCarriedIntoPlayerInventory();
		}
	}

	private static Slot findDestination(AbstractContainerMenu menu, ItemStack moving, Slot source, boolean sourceIsPlayer) {
		Slot empty = null;
		for (Slot slot : menu.slots) {
			if (slot == source || slot instanceof ResultSlot || slot.isFake() || isTakeOnlySlot(slot)) {
				continue;
			}

			boolean playerSlot = InventoryClicks.isPlayerInventorySlot(slot, Minecraft.getInstance());
			if (playerSlot == sourceIsPlayer) {
				continue;
			}

			ItemStack existing = slot.getItem();
			if (existing.isEmpty()) {
				if (empty == null && slot.mayPlace(moving)) {
					empty = slot;
				}

				continue;
			}

			if (slot.mayPlace(moving) && ItemStack.isSameItemSameComponents(existing, moving) && existing.getCount() < existing.getMaxStackSize()) {
				return slot;
			}
		}

		return empty;
	}

	private static boolean isTakeOnlySlot(Slot slot) {
		return slot.hasItem() && !slot.mayPlace(slot.getItem());
	}
}
