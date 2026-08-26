package dev.nifte.feature.autoweapon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;
import dev.nifte.feature.toolprotect.ToolProtectFeature;
import dev.nifte.inventory.InventoryClicks;

public final class AutoWeaponFeature {
	private AutoWeaponFeature() {
	}

	public static void toggle() {
		NifteConfig config = NifteConfig.get();
		config.autoWeaponEnabled = !config.autoWeaponEnabled;
		NifteConfig.save();

		Component message = Component.translatable(
			config.autoWeaponEnabled ? "nifte.auto_weapon.enabled" : "nifte.auto_weapon.disabled"
		);
		Minecraft.getInstance().gui.hud.setOverlayMessage(message, false);
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
		boolean healthy = !usable.nextDamageWillBreak();
		return new AutoWeaponScore(canReach, damage(player, usable, target), healthy);
	}

	private static float damage(LocalPlayer player, ItemStack stack, Entity target) {
		float damage = attackDamage(player, stack);
		float smash = stack.getItem().getAttackDamageBonus(target, damage, player.damageSources().playerAttack(player));
		if (smash > 0.0F) {
			int density = enchantmentLevel(stack, player.level(), Enchantments.DENSITY);
			if (density > 0) {
				smash += 0.5F * density * (float) player.fallDistance;
			}
		}

		return damage + smash + enchantmentDamage(stack, target, player.level());
	}

	private static float attackDamage(LocalPlayer player, ItemStack stack) {
		final double[] addValue = {0.0};
		final double[] multipliedBase = {0.0};
		final double[] multipliedTotal = {1.0};
		stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
			if (!attribute.equals(Attributes.ATTACK_DAMAGE)) {
				return;
			}

			if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
				addValue[0] += modifier.amount();
			} else if (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
				multipliedBase[0] += modifier.amount();
			} else if (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
				multipliedTotal[0] *= 1.0 + modifier.amount();
			}
		});
		double afterAdd = player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) + addValue[0];
		return (float) ((afterAdd + afterAdd * multipliedBase[0]) * multipliedTotal[0]);
	}

	private static float enchantmentDamage(ItemStack stack, Entity target, Level level) {
		if (stack.isEmpty()) {
			return 0.0F;
		}

		float damage = 0.0F;
		int sharpness = enchantmentLevel(stack, level, Enchantments.SHARPNESS);
		if (sharpness > 0) {
			damage += 1.0F + 0.5F * (sharpness - 1);
		}

		int smite = enchantmentLevel(stack, level, Enchantments.SMITE);
		if (smite > 0 && target.is(EntityTypeTags.SENSITIVE_TO_SMITE)) {
			damage += 2.5F * smite;
		}

		int bane = enchantmentLevel(stack, level, Enchantments.BANE_OF_ARTHROPODS);
		if (bane > 0 && target.is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
			damage += 2.5F * bane;
		}

		int impaling = enchantmentLevel(stack, level, Enchantments.IMPALING);
		if (impaling > 0 && target.is(EntityTypeTags.SENSITIVE_TO_IMPALING)) {
			damage += 2.5F * impaling;
		}

		return damage;
	}

	private static int enchantmentLevel(ItemStack stack, Level level, ResourceKey<Enchantment> enchantment) {
		Holder<Enchantment> holder = holder(level, enchantment);
		if (holder == null) {
			return 0;
		}

		return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
	}

	private static @Nullable Holder<Enchantment> holder(Level level, ResourceKey<Enchantment> enchantment) {
		return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(enchantment).orElse(null);
	}
}
