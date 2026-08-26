package dev.nifte.mixin;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.food.ClientFoodHungerTooltip;
import dev.nifte.feature.food.FoodHungerTitle;
import dev.nifte.feature.shulker.ClientShulkerBoxTooltip;
import dev.nifte.feature.shulker.ShulkerBoxTooltip;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {
	@Inject(
		method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void nifte$tooltipComponent(TooltipComponent component, CallbackInfoReturnable<ClientTooltipComponent> cir) {
		if (component instanceof ShulkerBoxTooltip tooltip) {
			cir.setReturnValue(new ClientShulkerBoxTooltip(tooltip));
			return;
		}

		if (component instanceof FoodHungerTitle title) {
			cir.setReturnValue(new ClientFoodHungerTooltip(Component.empty(), title.nutrition()));
		}
	}
}
