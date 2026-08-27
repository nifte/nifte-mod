package dev.nifte.mixin;

import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.config.NifteConfigScreen;

@Mixin(value = AbstractConfigEntry.class, remap = false)
public abstract class AbstractConfigEntryMixin {
	private static final int TOOLTIP_MAX_WIDTH = 200;

	@Shadow
	public abstract AbstractConfigScreen getConfigScreen();

	@Shadow
	protected abstract FormattedCharSequence[] wrapLines(Component[] lines, int width);

	@Redirect(
		method = "getDisplayedFieldName",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/ChatFormatting;GRAY:Lnet/minecraft/ChatFormatting;",
			opcode = Opcodes.GETSTATIC
		)
	)
	private ChatFormatting nifte$whiteSettingNames() {
		if (NifteConfigScreen.isNifteScreen(getConfigScreen())) {
			return ChatFormatting.WHITE;
		}

		return ChatFormatting.GRAY;
	}

	@Inject(method = "wrapLinesToScreen", at = @At("HEAD"), cancellable = true)
	private void nifte$narrowTooltips(Component[] lines, CallbackInfoReturnable<FormattedCharSequence[]> cir) {
		if (!NifteConfigScreen.isNifteScreen(getConfigScreen())) {
			return;
		}

		cir.setReturnValue(wrapLines(lines, TOOLTIP_MAX_WIDTH));
	}
}
