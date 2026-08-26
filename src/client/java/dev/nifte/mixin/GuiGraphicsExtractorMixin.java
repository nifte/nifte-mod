package dev.nifte.mixin;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.food.FoodHungerTitle;
import dev.nifte.feature.food.FoodHungerTooltips;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
	@Shadow
	protected abstract void setTooltipForNextFrameInternal(
		Font font,
		List<ClientTooltipComponent> lines,
		int xo,
		int yo,
		ClientTooltipPositioner positioner,
		@Nullable Identifier style,
		boolean replaceExisting
	);

	@Inject(
		method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void nifte$foodHungerTooltip(
		Font font,
		List<Component> texts,
		Optional<TooltipComponent> optionalImage,
		int xo,
		int yo,
		@Nullable Identifier style,
		CallbackInfo ci
	) {
		if (optionalImage.isEmpty() || !(optionalImage.get() instanceof FoodHungerTitle)) {
			return;
		}

		this.setTooltipForNextFrameInternal(
			font,
			FoodHungerTooltips.assemble(texts, optionalImage),
			xo,
			yo,
			DefaultTooltipPositioner.INSTANCE,
			style,
			false
		);
		ci.cancel();
	}
}
