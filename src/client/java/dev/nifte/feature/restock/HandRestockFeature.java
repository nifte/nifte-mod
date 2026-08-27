package dev.nifte.feature.restock;

import java.util.ArrayDeque;

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
	private static final ArrayDeque<ItemStack> usedStacks = new ArrayDeque<>();

	private HandRestockFeature() {
	}

	public static void capture(LocalPlayer player, InteractionHand hand) {
		usedStacks.addLast(player.getItemInHand(hand).copy());
	}

	public static void afterUse(LocalPlayer player, InteractionHand hand, @Nullable InteractionResult result) {
		ItemStack used = usedStacks.isEmpty() ? ItemStack.EMPTY : usedStacks.removeLast();
		if (result == null || !result.consumesAction() || !ranOut(used, player.getItemInHand(hand))) {
			return;
		}

		restock(player, hand, used);
	}

	private static boolean ranOut(ItemStack used, ItemStack remaining) {
		if (!HandRestockItems.isRestockable(used)) {
			return false;
		}

		return remaining.isEmpty() || !ItemStack.isSameItemSameComponents(remaining, used);
	}

	private static void restock(LocalPlayer player, InteractionHand hand, ItemStack template) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!canRestock(minecraft, player)) {
			return;
		}

		Slot source = findMatch(minecraft, template, destSlot(player, hand));
		if (source == null) {
			return;
		}

		if (hand == InteractionHand.OFF_HAND) {
			InventoryClicks.swapOffhand(source.index);
			return;
		}

		InventoryClicks.swapWithHotbar(source.index, player.getInventory().getSelectedSlot());
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
