package dev.nifte.feature.autototem;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

import dev.nifte.config.NifteConfig;
import dev.nifte.inventory.InventoryClicks;

public final class AutoTotemFeature {
	private static boolean refillPending;

	private AutoTotemFeature() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(AutoTotemFeature::tick);
	}

	public static void onTotemPopped() {
		if (NifteConfig.get().autoTotemEnabled) {
			refillPending = true;
		}
	}

	private static void tick(Minecraft minecraft) {
		if (!NifteConfig.get().autoTotemEnabled || !refillPending || minecraft.player == null || minecraft.gameMode == null) {
			return;
		}

		if (!minecraft.player.getOffhandItem().isEmpty()) {
			if (minecraft.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
				refillPending = false;
			}

			return;
		}

		if (!InventoryClicks.cursorEmpty()) {
			return;
		}

		if (minecraft.player.containerMenu != minecraft.player.inventoryMenu) {
			return;
		}

		for (Slot slot : minecraft.player.inventoryMenu.slots) {
			if (!InventoryClicks.isPlayerInventorySlot(slot, minecraft) || slot.getContainerSlot() == Inventory.SLOT_OFFHAND) {
				continue;
			}

			if (slot.getItem().is(Items.TOTEM_OF_UNDYING)) {
				InventoryClicks.swapOffhand(slot.index);
				refillPending = false;
				return;
			}
		}

		refillPending = false;
	}
}
