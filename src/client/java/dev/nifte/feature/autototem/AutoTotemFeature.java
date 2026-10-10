package dev.nifte.feature.autototem;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import dev.nifte.config.NifteConfig;
import dev.nifte.inventory.InventoryClicks;

public final class AutoTotemFeature {
	private static final int REFILL_ATTEMPT_TICKS = 40;

	private static boolean refillPending;
	private static int refillAttempts;
	private static InteractionHand refillHand = InteractionHand.OFF_HAND;
	private static int refillHotbarSlot;
	private static boolean mainHandHadTotem;
	private static boolean offhandHadTotem;

	private AutoTotemFeature() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(AutoTotemFeature::tick);
	}

	public static void onItemActivation(ItemStack stack) {
		if (!stack.is(Items.TOTEM_OF_UNDYING) || !NifteConfig.get().autoTotemEnabled) {
			return;
		}

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		refillHand = poppedHand(player, stack);
		refillHotbarSlot = player.getInventory().getSelectedSlot();
		refillAttempts = 0;
		refillPending = true;
	}

	private static void tick(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player != null && minecraft.gameMode != null && NifteConfig.get().autoTotemEnabled && refillPending) {
			refill(minecraft, player);
		}

		if (player != null) {
			mainHandHadTotem = holdsTotem(player.getMainHandItem());
			offhandHadTotem = holdsTotem(player.getOffhandItem());
		}
	}

	private static void refill(Minecraft minecraft, LocalPlayer player) {
		if (refillHand == InteractionHand.MAIN_HAND && !Inventory.isHotbarSlot(refillHotbarSlot)) {
			refillPending = false;
			return;
		}

		ItemStack held = heldStack(player);
		if (!held.isEmpty()) {
			// Elytra crashes and other player-tick damage sync the emptied hand a tick after the pop animation.
			if (++refillAttempts >= REFILL_ATTEMPT_TICKS) {
				refillPending = false;
			}

			return;
		}

		if (!InventoryClicks.cursorEmpty() || player.containerMenu != player.inventoryMenu) {
			return;
		}

		for (Slot slot : player.inventoryMenu.slots) {
			if (!InventoryClicks.isPlayerInventorySlot(slot, minecraft)) {
				continue;
			}

			int containerSlot = slot.getContainerSlot();
			if (containerSlot == Inventory.SLOT_OFFHAND || isRefillSlot(containerSlot)) {
				continue;
			}

			if (!slot.getItem().is(Items.TOTEM_OF_UNDYING)) {
				continue;
			}

			if (refillHand == InteractionHand.OFF_HAND) {
				InventoryClicks.swapOffhand(slot.index);
			} else {
				InventoryClicks.swapWithHotbar(slot.index, refillHotbarSlot);
			}

			refillPending = false;
			return;
		}

		refillPending = false;
	}

	private static InteractionHand poppedHand(LocalPlayer player, ItemStack stack) {
		for (InteractionHand hand : InteractionHand.values()) {
			if (player.getItemInHand(hand) == stack) {
				return hand;
			}
		}

		if (mainHandHadTotem && !holdsTotem(player.getMainHandItem())) {
			return InteractionHand.MAIN_HAND;
		}

		if (offhandHadTotem && !holdsTotem(player.getOffhandItem())) {
			return InteractionHand.OFF_HAND;
		}

		return InteractionHand.OFF_HAND;
	}

	private static ItemStack heldStack(LocalPlayer player) {
		if (refillHand == InteractionHand.OFF_HAND) {
			return player.getOffhandItem();
		}

		return player.getInventory().getItem(refillHotbarSlot);
	}

	private static boolean isRefillSlot(int containerSlot) {
		return refillHand == InteractionHand.MAIN_HAND && containerSlot == refillHotbarSlot;
	}

	private static boolean holdsTotem(ItemStack stack) {
		return !stack.isEmpty() && stack.is(Items.TOTEM_OF_UNDYING);
	}
}
