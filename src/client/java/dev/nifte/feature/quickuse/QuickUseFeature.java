package dev.nifte.feature.quickuse;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;

import dev.nifte.config.NifteConfig;
import dev.nifte.input.NifteKeybinds;

public final class QuickUseFeature {
	private static boolean active;
	private static boolean startedThisTick;
	private static int originalSlot = -1;
	private static int targetSlot = -1;

	private QuickUseFeature() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(QuickUseFeature::afterTick);
	}

	public static void beforeTick(Minecraft minecraft) {
		startedThisTick = false;
		if (!enabled() || !inPlay(minecraft)) {
			return;
		}

		LocalPlayer player = minecraft.player;
		if (player == null) {
			return;
		}

		if (active) {
			if (targetKeyDown()) {
				selectSlot(player, targetSlot);
			}

			return;
		}

		int held = firstHeldSlot();
		if (held < 0) {
			return;
		}

		originalSlot = player.getInventory().getSelectedSlot();
		targetSlot = held;
		active = true;
		startedThisTick = true;
		selectSlot(player, targetSlot);
	}

	public static boolean shouldSkipUseDelay() {
		return startedThisTick;
	}

	public static boolean shouldEmulateUse() {
		Minecraft minecraft = Minecraft.getInstance();
		return active && enabled() && targetKeyDown() && inPlay(minecraft);
	}

	public static boolean shouldEmulateUse(KeyMapping mapping) {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft != null && minecraft.options != null && mapping == minecraft.options.keyUse && shouldEmulateUse();
	}

	private static void afterTick(Minecraft minecraft) {
		if (!active) {
			return;
		}

		if (enabled() && inPlay(minecraft) && targetKeyDown()) {
			return;
		}

		if (minecraft.player != null && originalSlot >= 0 && originalSlot < Inventory.SELECTION_SIZE) {
			selectSlot(minecraft.player, originalSlot);
		}

		active = false;
		startedThisTick = false;
		originalSlot = -1;
		targetSlot = -1;
	}

	private static boolean enabled() {
		return NifteConfig.get().quickUseEnabled;
	}

	private static boolean inPlay(Minecraft minecraft) {
		return minecraft.player != null
			&& minecraft.gameMode != null
			&& !minecraft.isPaused()
			&& minecraft.gui.screen() == null
			&& minecraft.gui.overlay() == null
			&& minecraft.player.isAlive()
			&& !minecraft.player.isSpectator();
	}

	private static int firstHeldSlot() {
		KeyMapping[] keys = NifteKeybinds.quickUseSlots;
		if (keys == null) {
			return -1;
		}

		for (int slot = 0; slot < keys.length; slot++) {
			KeyMapping key = keys[slot];
			if (key != null && key.isDown()) {
				return slot;
			}
		}

		return -1;
	}

	private static boolean targetKeyDown() {
		KeyMapping[] keys = NifteKeybinds.quickUseSlots;
		if (keys == null || targetSlot < 0 || targetSlot >= keys.length) {
			return false;
		}

		KeyMapping key = keys[targetSlot];
		return key != null && key.isDown();
	}

	private static void selectSlot(LocalPlayer player, int slot) {
		Inventory inventory = player.getInventory();
		if (inventory.getSelectedSlot() == slot) {
			return;
		}

		inventory.setSelectedSlot(slot);
		player.connection.send(new ServerboundSetCarriedItemPacket(slot));
	}
}
