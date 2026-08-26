package dev.nifte.feature.restock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;

final class HandRestockItems {
	private HandRestockItems() {
	}

	static boolean isRestockable(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		Item item = stack.getItem();
		if (item instanceof BlockItem) {
			return true;
		}

		return item instanceof BucketItem && !(item instanceof MobBucketItem);
	}
}
