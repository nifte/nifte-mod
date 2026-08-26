package dev.nifte.feature.drag;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.NonInteractiveResultSlot;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;
import dev.nifte.inventory.InventoryClicks;
import dev.nifte.mixin.AbstractContainerScreenAccessor;

public final class ContainerDragFeature {
	private static final Set<Integer> shiftVisitedSlots = new HashSet<>();

	private static boolean shiftDragging;
	private static boolean rightDragging;
	private static int lastRightDepositSlot = -1;

	private ContainerDragFeature() {
	}

	public static boolean handleClick(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
		if (!enabledOn(screen)) {
			clear();
			return false;
		}

		Slot slot = slotAt(screen, event);
		if (event.button() == 0 && event.hasShiftDown() && InventoryClicks.cursorEmpty() && slot != null) {
			shiftDragging = true;
			shiftVisitedSlots.clear();
			shiftVisitedSlots.add(slot.index);
			return false;
		}

		if (event.button() != 1 || InventoryClicks.cursorEmpty() || slot == null) {
			return false;
		}

		rightDragging = true;
		lastRightDepositSlot = slot.index;
		tryDepositOne(screen.getMenu(), slot);
		return true;
	}

	public static boolean handleDrag(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
		if (!enabledOn(screen)) {
			return false;
		}

		Slot slot = slotAt(screen, event);
		if (rightDragging && event.button() == 1) {
			if (slot == null) {
				lastRightDepositSlot = -1;
				return true;
			}

			if (slot.index != lastRightDepositSlot) {
				lastRightDepositSlot = slot.index;
				tryDepositOne(screen.getMenu(), slot);
			}

			return true;
		}

		if (shiftDragging && event.button() == 0 && event.hasShiftDown()) {
			tryQuickMove(slot);
		}

		return false;
	}

	public static boolean handleRelease(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
		boolean cancelVanilla = rightDragging && event.button() == 1 && enabledOn(screen);
		clear();
		return cancelVanilla;
	}

	private static boolean enabledOn(AbstractContainerScreen<?> screen) {
		return NifteConfig.get().containerDragEnabled && !(screen instanceof CreativeModeInventoryScreen);
	}

	private static @Nullable Slot slotAt(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
		return ((AbstractContainerScreenAccessor) screen).nifte$getHoveredSlot(event.x(), event.y());
	}

	private static void tryQuickMove(@Nullable Slot slot) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player == null || slot == null || !isInteractable(slot) || !slot.hasItem() || !slot.mayPickup(player)) {
			return;
		}

		if (!shiftVisitedSlots.add(slot.index)) {
			return;
		}

		InventoryClicks.quickMove(slot.index);
	}

	private static void tryDepositOne(AbstractContainerMenu menu, Slot slot) {
		ItemStack carried = menu.getCarried();
		if (carried.isEmpty() || !isInteractable(slot) || !slot.mayPlace(carried) || !menu.canDragTo(slot)) {
			return;
		}

		if (!AbstractContainerMenu.canItemQuickReplace(slot, carried, true)) {
			return;
		}

		int maxCount = Math.min(carried.getMaxStackSize(), slot.getMaxStackSize(carried));
		if (slot.hasItem() && slot.getItem().getCount() >= maxCount) {
			return;
		}

		InventoryClicks.pickupOne(slot.index);
	}

	private static boolean isInteractable(Slot slot) {
		return slot.isActive()
			&& !slot.isFake()
			&& !(slot instanceof ResultSlot)
			&& !(slot instanceof FurnaceResultSlot)
			&& !(slot instanceof MerchantResultSlot)
			&& !(slot instanceof NonInteractiveResultSlot);
	}

	private static void clear() {
		shiftDragging = false;
		rightDragging = false;
		lastRightDepositSlot = -1;
		shiftVisitedSlots.clear();
	}
}
