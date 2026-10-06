package dev.nifte.feature.autotool;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import dev.nifte.config.NifteConfig;
import dev.nifte.feature.combat.MeleeDamage;
import dev.nifte.feature.toolprotect.ToolProtectFeature;

final class AutoToolEntities {
	private AutoToolEntities() {
	}

	static void selectFor(Minecraft minecraft, Entity target) {
		NifteConfig config = NifteConfig.get();
		if (!config.autoToolEnabled || minecraft.player == null || minecraft.level == null || minecraft.gui.screen() != null) {
			return;
		}

		LocalPlayer player = minecraft.player;
		if (player.getAbilities().instabuild || player.isSpectator() || player.isHandsBusy()) {
			return;
		}

		if (!(target instanceof VehicleEntity) || !target.isAlive() || !target.isAttackable()) {
			return;
		}

		Inventory inventory = player.getInventory();
		int currentSlot = inventory.getSelectedSlot();
		int slotCount = config.autoToolFromInventory ? Inventory.INVENTORY_SIZE : Inventory.SELECTION_SIZE;
		Vec3 hitLocation = minecraft.hitResult == null ? target.position() : minecraft.hitResult.getLocation();
		int bestSlot = AutoToolSlots.bestSlot(inventory, currentSlot, slotCount, stack -> score(player, stack, target, hitLocation));
		AutoToolSlots.swap(minecraft, player, bestSlot, currentSlot);
	}

	private static AutoToolEntityScore score(LocalPlayer player, ItemStack stack, Entity target, Vec3 hitLocation) {
		ItemStack usable = ToolProtectFeature.shouldBlock(stack) ? ItemStack.EMPTY : stack;
		boolean canReach = player.getAttackRangeWith(usable).isInRange(player, hitLocation);
		float damage = MeleeDamage.damage(player, usable, target);
		float attackSpeed = MeleeDamage.attackSpeed(player, usable);
		return new AutoToolEntityScore(canReach, VehicleBreakTime.ticksUntilBreak(damage, attackSpeed), damage);
	}
}
