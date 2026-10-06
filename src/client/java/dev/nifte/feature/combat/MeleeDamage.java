package dev.nifte.feature.combat;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.Nullable;

public final class MeleeDamage {
	private MeleeDamage() {
	}

	public static float damage(LocalPlayer player, ItemStack stack, Entity target) {
		float damage = attributeValue(player, stack, Attributes.ATTACK_DAMAGE);
		float smash = stack.getItem().getAttackDamageBonus(target, damage, player.damageSources().playerAttack(player));
		if (smash > 0.0F) {
			int density = enchantmentLevel(stack, player.level(), Enchantments.DENSITY);
			if (density > 0) {
				smash += 0.5F * density * (float) player.fallDistance;
			}
		}

		return damage + smash + enchantmentDamage(stack, target, player.level());
	}

	public static float attackSpeed(LocalPlayer player, ItemStack stack) {
		return attributeValue(player, stack, Attributes.ATTACK_SPEED);
	}

	private static float attributeValue(LocalPlayer player, ItemStack stack, Holder<Attribute> attribute) {
		final double[] addValue = {0.0};
		final double[] multipliedBase = {0.0};
		final double[] multipliedTotal = {1.0};
		stack.forEachModifier(EquipmentSlot.MAINHAND, (type, modifier) -> {
			if (!type.equals(attribute)) {
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
		double afterAdd = player.getAttributeBaseValue(attribute) + addValue[0];
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
