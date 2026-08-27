package dev.nifte.feature.overlay;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import dev.nifte.config.NifteConfig;

public final class HideHeldTotemFeature {
	private HideHeldTotemFeature() {
	}

	public static boolean shouldHide(ItemStack stack) {
		return NifteConfig.get().hideHeldTotem && stack.is(Items.TOTEM_OF_UNDYING);
	}
}
