package dev.nifte.feature.autoweapon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import dev.nifte.config.NifteConfig;
import dev.nifte.feature.combat.MeleeDamage;
import dev.nifte.feature.toolprotect.ToolProtectFeature;
import dev.nifte.hud.ToggleOverlay;
import dev.nifte.inventory.InventoryClicks;

public final class AutoWeaponFeature {
	private AutoWeaponFeature() {
	}

	public static void toggle() {
		NifteConfig config = NifteConfig.get();
		config.autoWeaponEnabled = !config.autoWeaponEnabled;
		NifteConfig.save();

		ToggleOverlay.show("nifte.auto_weapon.toggle", config.autoWeaponEnabled);
	}

	public static void selectFor(Minecraft minecraft, Entity target) {
		NifteConfig config = NifteConfig.get();
		if (!config.autoWeaponEnabled || minecraft.player == null || minecraft.level == null || minecraft.gui.screen() != null) {
			return;
		}

		LocalPlayer player = minecraft.player;
		if (player.getAbilities().instabuild || player.isSpectator() || player.isHandsBusy() || !isCombatTarget(target)) {
			return;
		}

		Inventory inventory = player.getInventory();
		int currentSlot = inventory.getSelectedSlot();
		int slotCount = config.autoWeaponFromInventory ? Inventory.INVENTORY_SIZE : Inventory.SELECTION_SIZE;
		Vec3 hitLocation = minecraft.hitResult == null ? target.position() : minecraft.hitResult.getLocation();
		AutoWeaponScore bestScore = score(player, inventory.getItem(currentSlot), target, hitLocation);
		int bestSlot = currentSlot;
		for (int slot = 0; slot < slotCount; slot++) {
			if (slot == currentSlot) {
				continue;
			}

			AutoWeaponScore candidate = score(player, inventory.getItem(slot), target, hitLocation);
			if (candidate.isBetterThan(bestScore)) {
				bestScore = candidate;
				bestSlot = slot;
			}
		}

		if (bestSlot == currentSlot) {
			return;
		}

		if (bestSlot < Inventory.SELECTION_SIZE) {
			inventory.setSelectedSlot(bestSlot);
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

	private static boolean isCombatTarget(Entity target) {
		return target.isAttackable() && (target instanceof LivingEntity || target instanceof EnderDragonPart);
	}

	private static AutoWeaponScore score(LocalPlayer player, ItemStack stack, Entity target, Vec3 hitLocation) {
		ItemStack usable = ToolProtectFeature.shouldBlock(stack) ? ItemStack.EMPTY : stack;
		boolean canReach = player.getAttackRangeWith(usable).isInRange(player, hitLocation);
		return new AutoWeaponScore(canReach, MeleeDamage.damage(player, usable, target));
	}
}
