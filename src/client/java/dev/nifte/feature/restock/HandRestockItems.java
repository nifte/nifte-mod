package dev.nifte.feature.restock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SolidBucketItem;

import dev.nifte.config.NifteConfig;

final class HandRestockItems {
	private HandRestockItems() {
	}

	static boolean isRestockable(ItemStack stack) {
		NifteConfig config = NifteConfig.get();
		if (isBlock(stack)) {
			return config.blockRestockEnabled;
		}

		if (isLiquidBucket(stack)) {
			return config.bucketRestockEnabled;
		}

		return isConsumable(stack) && config.consumableRestockEnabled;
	}

	static boolean isConsumable(ItemStack stack) {
		return isPotion(stack) || isStew(stack);
	}

	private static boolean isPotion(ItemStack stack) {
		return stack.getItem() instanceof PotionItem;
	}

	private static boolean isStew(ItemStack stack) {
		return stack.is(Items.MUSHROOM_STEW)
			|| stack.is(Items.RABBIT_STEW)
			|| stack.is(Items.BEETROOT_SOUP)
			|| stack.is(Items.SUSPICIOUS_STEW);
	}

	private static boolean isBlock(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof BlockItem && !(item instanceof SolidBucketItem);
	}

	private static boolean isLiquidBucket(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof SolidBucketItem) {
			return true;
		}

		return item instanceof BucketItem && !(item instanceof MobBucketItem);
	}
}
