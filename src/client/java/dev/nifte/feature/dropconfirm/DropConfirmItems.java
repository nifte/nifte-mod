package dev.nifte.feature.dropconfirm;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

final class DropConfirmItems {
	private DropConfirmItems() {
	}

	static boolean isToolOrWeapon(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		if (stack.getItem() instanceof ProjectileWeaponItem
			|| stack.has(DataComponents.TOOL)
			|| stack.has(DataComponents.WEAPON)
			|| stack.has(DataComponents.PIERCING_WEAPON)
			|| stack.has(DataComponents.KINETIC_WEAPON)) {
			return true;
		}

		return stack.is(ItemTags.SWORDS)
			|| stack.is(ItemTags.AXES)
			|| stack.is(ItemTags.PICKAXES)
			|| stack.is(ItemTags.SHOVELS)
			|| stack.is(ItemTags.HOES)
			|| stack.is(ItemTags.SPEARS)
			|| stack.is(ItemTags.WEAPON_ENCHANTABLE)
			|| stack.is(ItemTags.MINING_ENCHANTABLE)
			|| stack.is(ItemTags.SHARP_WEAPON_ENCHANTABLE)
			|| stack.is(ItemTags.MELEE_WEAPON_ENCHANTABLE)
			|| stack.is(ItemTags.BOW_ENCHANTABLE)
			|| stack.is(ItemTags.CROSSBOW_ENCHANTABLE);
	}
}
