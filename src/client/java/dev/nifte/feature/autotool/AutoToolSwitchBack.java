package dev.nifte.feature.autotool;

import java.util.ArrayDeque;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import dev.nifte.config.NifteConfig;
import dev.nifte.inventory.InventoryClicks;

final class AutoToolSwitchBack {
	private static final int RETURN_DELAY_TICKS = 10;
	private static final int GIVE_UP_TICKS = 20;

	private static final ArrayDeque<MovedTool> moved = new ArrayDeque<>();
	private static int returnSlot = -1;
	private static int toolSlot = -1;
	private static int idleTicks;
	private static boolean mining;

	private AutoToolSwitchBack() {
	}

	static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(AutoToolSwitchBack::tick);
	}

	static void markMining() {
		mining = true;
	}

	static void record(int selectedSlot, int bestSlot, ItemStack tool, ItemStack displaced) {
		if (returnSlot < 0) {
			returnSlot = selectedSlot;
		}

		idleTicks = 0;
		if (bestSlot < Inventory.SELECTION_SIZE) {
			toolSlot = bestSlot;
		} else {
			toolSlot = selectedSlot;
			moved.addLast(new MovedTool(bestSlot, selectedSlot, tool, displaced));
		}

		if (toolSlot == returnSlot && moved.isEmpty()) {
			clear();
		}
	}

	private static void tick(Minecraft minecraft) {
		boolean wasMining = mining;
		mining = false;
		if (returnSlot < 0) {
			return;
		}

		if (minecraft.gameMode != null && minecraft.gameMode.isDestroying()) {
			wasMining = true;
		}

		NifteConfig config = NifteConfig.get();
		if (!config.autoToolEnabled || !config.autoToolSwitchBack) {
			clear();
			return;
		}

		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.level == null || player.isSpectator() || minecraft.gameMode == null) {
			clear();
			return;
		}

		if (wasMining) {
			idleTicks = 0;
			return;
		}

		if (minecraft.gui.screen() != null || player.isUsingItem()) {
			return;
		}

		idleTicks++;
		if (idleTicks < RETURN_DELAY_TICKS) {
			return;
		}

		if (!moved.isEmpty() && !canSwap(player)) {
			if (idleTicks >= RETURN_DELAY_TICKS + GIVE_UP_TICKS) {
				clear();
			}

			return;
		}

		restore(minecraft, player);
		clear();
	}

	private static void restore(Minecraft minecraft, LocalPlayer player) {
		while (!moved.isEmpty()) {
			restoreMove(minecraft, player, moved.removeLast());
		}

		if (returnSlot < 0 || returnSlot >= Inventory.SELECTION_SIZE || toolSlot == returnSlot) {
			return;
		}

		if (player.getInventory().getSelectedSlot() == toolSlot) {
			AutoToolSlots.select(player, returnSlot);
		}
	}

	private static void restoreMove(Minecraft minecraft, LocalPlayer player, MovedTool move) {
		if (move.hotbarSlot < 0 || move.hotbarSlot >= Inventory.SELECTION_SIZE) {
			return;
		}

		Inventory inventory = player.getInventory();
		ItemStack hotbar = inventory.getItem(move.hotbarSlot);
		ItemStack source = inventory.getItem(move.inventorySlot);
		if (!stillHoldsTool(hotbar, move.tool) || !stillDisplaced(source, move.displaced)) {
			return;
		}

		int menuSlot = InventoryClicks.playerMenuSlot(minecraft, move.inventorySlot);
		if (menuSlot >= 0) {
			InventoryClicks.swapWithHotbar(menuSlot, move.hotbarSlot);
		}
	}

	private static boolean stillHoldsTool(ItemStack stack, ItemStack tool) {
		if (tool.isEmpty()) {
			return stack.isEmpty();
		}

		return stack.isEmpty() || ItemStack.isSameItem(stack, tool);
	}

	private static boolean stillDisplaced(ItemStack stack, ItemStack displaced) {
		if (displaced.isEmpty()) {
			return stack.isEmpty();
		}

		return stack.getCount() == displaced.getCount() && ItemStack.isSameItemSameComponents(stack, displaced);
	}

	private static boolean canSwap(LocalPlayer player) {
		return player.containerMenu == player.inventoryMenu && InventoryClicks.cursorEmpty();
	}

	private static void clear() {
		moved.clear();
		returnSlot = -1;
		toolSlot = -1;
		idleTicks = 0;
	}

	private static final class MovedTool {
		private final int inventorySlot;
		private final int hotbarSlot;
		private final ItemStack tool;
		private final ItemStack displaced;

		private MovedTool(int inventorySlot, int hotbarSlot, ItemStack tool, ItemStack displaced) {
			this.inventorySlot = inventorySlot;
			this.hotbarSlot = hotbarSlot;
			this.tool = tool;
			this.displaced = displaced;
		}
	}
}
