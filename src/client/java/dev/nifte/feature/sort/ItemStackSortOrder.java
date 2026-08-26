package dev.nifte.feature.sort;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

final class ItemStackSortOrder {
	private ItemStackSortOrder() {
	}

	static int compare(ItemStack left, ItemStack right) {
		boolean leftEmpty = left.isEmpty();
		boolean rightEmpty = right.isEmpty();
		if (leftEmpty || rightEmpty) {
			return Boolean.compare(leftEmpty, rightEmpty);
		}

		int itemOrder = compareIdentifiers(BuiltInRegistries.ITEM.getKey(left.getItem()), BuiltInRegistries.ITEM.getKey(right.getItem()));
		if (itemOrder != 0) {
			return itemOrder;
		}

		int componentOrder = left.getComponentsPatch().toString().compareTo(right.getComponentsPatch().toString());
		if (componentOrder != 0) {
			return componentOrder;
		}

		return Integer.compare(right.getCount(), left.getCount());
	}

	private static int compareIdentifiers(@Nullable Identifier left, @Nullable Identifier right) {
		if (left == null && right == null) {
			return 0;
		}

		if (left == null) {
			return 1;
		}

		if (right == null) {
			return -1;
		}

		return left.toString().compareTo(right.toString());
	}
}
