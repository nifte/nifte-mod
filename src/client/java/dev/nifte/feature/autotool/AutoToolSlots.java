package dev.nifte.feature.autotool;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import dev.nifte.inventory.InventoryClicks;

final class AutoToolSlots {
	private AutoToolSlots() {
	}

	static <T extends Comparable<T>> int bestSlot(Inventory inventory, int currentSlot, int slotCount, Function<ItemStack, T> score) {
		T best = score.apply(inventory.getItem(currentSlot));
		int bestSlot = currentSlot;
		for (int slot = 0; slot < slotCount; slot++) {
			if (slot == currentSlot) {
				continue;
			}

			T candidate = score.apply(inventory.getItem(slot));
			if (candidate.compareTo(best) > 0) {
				best = candidate;
				bestSlot = slot;
			}
		}

		int offhand = Inventory.SLOT_OFFHAND;
		if (offhand != currentSlot) {
			T candidate = score.apply(inventory.getItem(offhand));
			if (candidate.compareTo(best) > 0) {
				bestSlot = offhand;
			}
		}

		return bestSlot;
	}

	static void swap(Minecraft minecraft, LocalPlayer player, int bestSlot, int currentSlot) {
		if (bestSlot == currentSlot) {
			return;
		}

		if (bestSlot < Inventory.SELECTION_SIZE) {
			player.getInventory().setSelectedSlot(bestSlot);
			player.connection.send(new ServerboundSetCarriedItemPacket(bestSlot));
			return;
		}

		if (player.containerMenu != player.inventoryMenu || !InventoryClicks.cursorEmpty()) {
			return;
		}

		int menuSlot = InventoryClicks.playerMenuSlot(minecraft, bestSlot);
		if (menuSlot >= 0) {
			InventoryClicks.swapWithHotbar(menuSlot, currentSlot);
		}
	}
}
