package dev.nifte.mixin;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.food.FoodHungerName;
import dev.nifte.feature.shulker.ShulkerBoxTooltips;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Inject(method = "getTooltipImage", at = @At("RETURN"), cancellable = true)
	private void nifte$tooltipImages(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
		if (cir.getReturnValue().isPresent()) {
			return;
		}

		ItemStack stack = (ItemStack) (Object) this;
		Optional<TooltipComponent> shulker = ShulkerBoxTooltips.image(stack);
		if (shulker.isPresent()) {
			cir.setReturnValue(shulker);
			return;
		}

		Optional<TooltipComponent> hunger = FoodHungerName.image(stack);
		if (hunger.isPresent()) {
			cir.setReturnValue(hunger);
		}
	}

	@Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
	private void nifte$hideShulkerContainerText(
		DataComponentType<?> type,
		Item.TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> consumer,
		TooltipFlag flag,
		CallbackInfo ci
	) {
		ItemStack stack = (ItemStack) (Object) this;
		if (type == DataComponents.CONTAINER && ShulkerBoxTooltips.hidesVanillaList(stack)) {
			ShulkerBoxTooltips.emptyLine(stack).ifPresent(consumer);
			ci.cancel();
		}
	}
}
