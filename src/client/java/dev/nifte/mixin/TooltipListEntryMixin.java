package dev.nifte.mixin;

import java.util.Optional;

import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;

import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.config.ConfigEntryControls;
import dev.nifte.config.NifteConfigScreen;

@Mixin(value = TooltipListEntry.class, remap = false)
public abstract class TooltipListEntryMixin {
	@Inject(method = "getTooltip(II)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
	private void nifte$hideTooltipOverControls(int mouseX, int mouseY, CallbackInfoReturnable<Optional<Component[]>> cir) {
		TooltipListEntry<?> entry = (TooltipListEntry<?>) (Object) this;
		if (!NifteConfigScreen.isNifteScreen(entry.getConfigScreen())) {
			return;
		}

		if (!ConfigEntryControls.isLeftOfControls(entry.children(), mouseX)) {
			cir.setReturnValue(Optional.empty());
		}
	}
}
