package dev.nifte.feature.elytra;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.Nullable;

import dev.nifte.inventory.InventoryClicks;

final class Chestplates {
	private Chestplates() {
	}

	static boolean isChestplate(ItemStack stack) {
		if (stack.isEmpty() || stack.has(DataComponents.GLIDER)) {
			return false;
		}

		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		return equippable != null && equippable.slot() == EquipmentSlot.CHEST;
	}

	static @Nullable Slot bestInInventory(Minecraft minecraft) {
		if (minecraft.player == null) {
			return null;
		}

		Level level = minecraft.player.level();
		Slot bestSlot = null;
		ChestplateScore bestScore = null;
		for (Slot slot : minecraft.player.inventoryMenu.slots) {
			if (!isInventoryChestplateSlot(slot, minecraft)) {
				continue;
			}

			ItemStack stack = slot.getItem();
			if (!isChestplate(stack)) {
				continue;
			}

			ChestplateScore score = score(stack, level);
			if (bestScore == null || score.isBetterThan(bestScore)) {
				bestScore = score;
				bestSlot = slot;
			}
		}

		return bestSlot;
	}

	private static boolean isInventoryChestplateSlot(Slot slot, Minecraft minecraft) {
		if (!InventoryClicks.isPlayerInventorySlot(slot, minecraft)) {
			return false;
		}

		int containerSlot = slot.getContainerSlot();
		return InventoryClicks.isPlayerMainInventorySlot(slot, minecraft) || containerSlot == Inventory.SLOT_OFFHAND;
	}

	private static ChestplateScore score(ItemStack stack, Level level) {
		final double[] armor = {0.0};
		final double[] toughness = {0.0};
		stack.forEachModifier(EquipmentSlot.CHEST, (attribute, modifier) -> {
			if (modifier.operation() != AttributeModifier.Operation.ADD_VALUE) {
				return;
			}

			if (attribute.equals(Attributes.ARMOR)) {
				armor[0] += modifier.amount();
			} else if (attribute.equals(Attributes.ARMOR_TOUGHNESS)) {
				toughness[0] += modifier.amount();
			}
		});
		int remaining = stack.isDamageableItem() ? stack.getMaxDamage() - stack.getDamageValue() : Integer.MAX_VALUE;
		return new ChestplateScore(armor[0], toughness[0], protection(stack, level), !stack.nextDamageWillBreak(), remaining);
	}

	private static int protection(ItemStack stack, Level level) {
		return enchantmentLevel(stack, level, Enchantments.PROTECTION)
			+ enchantmentLevel(stack, level, Enchantments.PROJECTILE_PROTECTION)
			+ enchantmentLevel(stack, level, Enchantments.BLAST_PROTECTION)
			+ enchantmentLevel(stack, level, Enchantments.FIRE_PROTECTION);
	}

	private static int enchantmentLevel(ItemStack stack, Level level, ResourceKey<Enchantment> enchantment) {
		Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(enchantment).orElse(null);
		if (holder == null) {
			return 0;
		}

		return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
	}
}
