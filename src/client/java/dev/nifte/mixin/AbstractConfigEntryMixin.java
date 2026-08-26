package dev.nifte.mixin;

import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;

import net.minecraft.ChatFormatting;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.nifte.config.NifteConfigScreen;

@Mixin(value = AbstractConfigEntry.class, remap = false)
public abstract class AbstractConfigEntryMixin {
	@Shadow
	public abstract AbstractConfigScreen getConfigScreen();

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
}
