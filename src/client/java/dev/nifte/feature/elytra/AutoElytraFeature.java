package dev.nifte.feature.elytra;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;
import dev.nifte.inventory.InventoryClicks;

public final class AutoElytraFeature {
	private static final int CHEST_SLOT = InventoryMenu.ARMOR_SLOT_START + 1;

	private static boolean wasAirborne;
	private static boolean wasJumpDown;
	private static boolean wasChestGlider;
	private static boolean restorePending;

	private AutoElytraFeature() {
	}

	public static void register() {
		ClientTickEvents.START_CLIENT_TICK.register(AutoElytraFeature::tick);
	}

	private static void tick(Minecraft minecraft) {
		boolean jumpDown = minecraft.options != null && minecraft.options.keyJump.isDown();
		if (!NifteConfig.get().autoElytraEnabled || minecraft.player == null || minecraft.gameMode == null) {
			wasJumpDown = jumpDown;
			wasChestGlider = false;
			restorePending = false;
			return;
		}

		LocalPlayer player = minecraft.player;
		boolean onGround = player.onGround();
		boolean chestGlider = isChestGlider(minecraft);
		if ((wasChestGlider && !chestGlider && needsChestplate(minecraft)) || (wasAirborne && onGround && chestGlider)) {
			restorePending = true;
		}

		if (restorePending) {
			if (!needsChestplate(minecraft)) {
				restorePending = false;
			} else if ((onGround || !chestGlider) && equipBestChestplate(minecraft)) {
				restorePending = false;
			}
		}

		boolean airborne = !onGround
			&& !player.isInWater()
			&& !player.getAbilities().flying
			&& player.getVehicle() == null;
		boolean jumpPressed = jumpDown && !wasJumpDown;
		if (airborne && !player.isFallFlying() && jumpPressed) {
			tryTakeoff(minecraft);
		}

		wasAirborne = airborne;
		wasJumpDown = jumpDown;
		wasChestGlider = isChestGlider(minecraft);
	}

	private static void tryTakeoff(Minecraft minecraft) {
		if (!isChestGlider(minecraft) && !equipElytra(minecraft)) {
			return;
		}

		startGliding(minecraft.player);
	}

	private static void startGliding(LocalPlayer player) {
		if (player.isFallFlying() || player.onGround()) {
			return;
		}

		if (player.tryToStartFallFlying()) {
			player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
		}
	}

	private static boolean equipElytra(Minecraft minecraft) {
		if (minecraft.player.containerMenu != minecraft.player.inventoryMenu || !InventoryClicks.cursorEmpty()) {
			return false;
		}

		Slot elytraSlot = findElytra(minecraft);
		if (elytraSlot == null) {
			return false;
		}

		InventoryClicks.pickup(elytraSlot.index);
		InventoryClicks.pickup(CHEST_SLOT);
		if (!InventoryClicks.cursorEmpty()) {
			InventoryClicks.pickup(elytraSlot.index);
		}

		return isChestGlider(minecraft);
	}

	private static boolean needsChestplate(Minecraft minecraft) {
		return !Chestplates.isChestplate(minecraft.player.getItemBySlot(EquipmentSlot.CHEST));
	}

	private static boolean equipBestChestplate(Minecraft minecraft) {
		if (minecraft.player.containerMenu != minecraft.player.inventoryMenu || !InventoryClicks.cursorEmpty()) {
			return false;
		}

		Slot chestplate = Chestplates.bestInInventory(minecraft);
		if (chestplate == null) {
			return true;
		}

		InventoryClicks.pickup(chestplate.index);
		InventoryClicks.pickup(CHEST_SLOT);
		if (!InventoryClicks.cursorEmpty()) {
			InventoryClicks.pickup(chestplate.index);
		}

		return Chestplates.isChestplate(minecraft.player.getItemBySlot(EquipmentSlot.CHEST));
	}

	private static boolean isChestGlider(Minecraft minecraft) {
		return LivingEntity.canGlideUsing(minecraft.player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST);
	}

	private static @Nullable Slot findElytra(Minecraft minecraft) {
		for (Slot slot : minecraft.player.inventoryMenu.slots) {
			if (slot.index == CHEST_SLOT || !InventoryClicks.isPlayerInventorySlot(slot, minecraft)) {
				continue;
			}

			if (LivingEntity.canGlideUsing(slot.getItem(), EquipmentSlot.CHEST)) {
				return slot;
			}
		}

		return null;
	}
}
