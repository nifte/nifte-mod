package dev.nifte.feature.restock;

import java.util.ArrayDeque;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import dev.nifte.inventory.InventoryClicks;

public final class HandRestockFeature {
	private static final int PENDING_CANCEL_TICKS = 20;
	private static final ArrayDeque<ItemStack> usedStacks = new ArrayDeque<>();

	private static ItemStack pendingTemplate = ItemStack.EMPTY;
	private static InteractionHand pendingHand = InteractionHand.MAIN_HAND;
	private static int pendingDestSlot = -1;
	private static int pendingIdleTicks;

	private HandRestockFeature() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(HandRestockFeature::tick);
	}

	public static void capture(LocalPlayer player, InteractionHand hand) {
		usedStacks.addLast(player.getItemInHand(hand).copy());
	}

	public static void afterUse(LocalPlayer player, InteractionHand hand, @Nullable InteractionResult result) {
		ItemStack used = usedStacks.isEmpty() ? ItemStack.EMPTY : usedStacks.removeLast();
		if (result == null || !result.consumesAction()) {
			return;
		}

		ItemStack remaining = player.getItemInHand(hand);
		if (ranOut(used, remaining)) {
			clearPending();
			restock(player, hand, used, destSlot(player, hand));
			return;
		}

		if (HandRestockItems.isRestockable(used) && HandRestockItems.isConsumable(used) && player.isUsingItem()) {
			pendingTemplate = used;
			pendingHand = hand;
			pendingDestSlot = destSlot(player, hand);
			pendingIdleTicks = 0;
		}
	}

	private static void tick(Minecraft minecraft) {
		if (pendingDestSlot < 0 || pendingTemplate.isEmpty()) {
			return;
		}

		LocalPlayer player = minecraft.player;
		if (player == null || player.hasInfiniteMaterials() || player.isSpectator()) {
			clearPending();
			return;
		}

		if (!HandRestockItems.isRestockable(pendingTemplate)) {
			clearPending();
			return;
		}

		if (player.isUsingItem()) {
			pendingIdleTicks = 0;
			return;
		}

		ItemStack remaining = stackInDest(player, pendingHand, pendingDestSlot);
		if (!ranOut(pendingTemplate, remaining)) {
			pendingIdleTicks++;
			if (pendingIdleTicks >= PENDING_CANCEL_TICKS) {
				clearPending();
			}

			return;
		}

		if (!canRestock(minecraft, player)) {
			return;
		}

		restock(player, pendingHand, pendingTemplate, pendingDestSlot);
		clearPending();
	}

	private static boolean ranOut(ItemStack used, ItemStack remaining) {
		if (!HandRestockItems.isRestockable(used)) {
			return false;
		}

		return remaining.isEmpty() || !ItemStack.isSameItemSameComponents(remaining, used);
	}

	private static void restock(LocalPlayer player, InteractionHand hand, ItemStack template, int destContainerSlot) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!canRestock(minecraft, player)) {
			return;
		}

		Slot source = findMatch(minecraft, template, destContainerSlot);
		if (source == null) {
			return;
		}

		if (hand == InteractionHand.OFF_HAND) {
			InventoryClicks.swapOffhand(source.index);
			return;
		}

		InventoryClicks.swapWithHotbar(source.index, destContainerSlot);
	}

	private static boolean canRestock(Minecraft minecraft, LocalPlayer player) {
		if (minecraft.gameMode == null) {
			return false;
		}

		if (player.hasInfiniteMaterials() || player.isSpectator()) {
			return false;
		}

		return player.containerMenu == player.inventoryMenu && InventoryClicks.cursorEmpty();
	}

	private static int destSlot(LocalPlayer player, InteractionHand hand) {
		return hand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : player.getInventory().getSelectedSlot();
	}

	private static ItemStack stackInDest(LocalPlayer player, InteractionHand hand, int destContainerSlot) {
		if (hand == InteractionHand.OFF_HAND) {
			return player.getOffhandItem();
		}

		return player.getInventory().getItem(destContainerSlot);
	}

	private static void clearPending() {
		pendingTemplate = ItemStack.EMPTY;
		pendingDestSlot = -1;
		pendingIdleTicks = 0;
	}

	private static @Nullable Slot findMatch(Minecraft minecraft, ItemStack template, int destContainerSlot) {
		Slot match = findMatch(minecraft, template, destContainerSlot, true);
		return match != null ? match : findMatch(minecraft, template, destContainerSlot, false);
	}

	private static @Nullable Slot findMatch(Minecraft minecraft, ItemStack template, int destContainerSlot, boolean inventory) {
		Slot best = null;
		for (Slot slot : minecraft.player.inventoryMenu.slots) {
			if (!InventoryClicks.isPlayerMainInventorySlot(slot, minecraft)) {
				continue;
			}

			int containerSlot = slot.getContainerSlot();
			if (containerSlot == destContainerSlot || (containerSlot < Inventory.SELECTION_SIZE) == inventory) {
				continue;
			}

			ItemStack stack = slot.getItem();
			if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, template)) {
				continue;
			}

			if (best == null || stack.getCount() > best.getItem().getCount()) {
				best = slot;
			}
		}

		return best;
	}
}
