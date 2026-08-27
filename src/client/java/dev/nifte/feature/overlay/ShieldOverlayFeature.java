package dev.nifte.feature.overlay;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

import dev.nifte.config.NifteConfig;

public final class ShieldOverlayFeature {
	public static final float MIN_OFFSET = 0.0F;
	public static final float MAX_OFFSET = 0.8F;
	public static final float DEFAULT_OFFSET = 0.3F;

	private ShieldOverlayFeature() {
	}

	public static void apply(PoseStack poseStack, ItemStack stack) {
		if (!(stack.getItem() instanceof ShieldItem)) {
			return;
		}

		NifteConfig config = NifteConfig.get();
		if (!config.lowerShield) {
			return;
		}

		poseStack.translate(0.0F, -config.shieldOffset, 0.0F);
	}
}
