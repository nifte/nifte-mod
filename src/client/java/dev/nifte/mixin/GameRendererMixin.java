package dev.nifte.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.autototem.AutoTotemFeature;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Inject(method = "displayItemActivation", at = @At("TAIL"))
	private void nifte$autoTotem(ItemStack itemStack, CallbackInfo ci) {
		if (itemStack.is(Items.TOTEM_OF_UNDYING)) {
			AutoTotemFeature.onTotemPopped();
		}
	}
}
