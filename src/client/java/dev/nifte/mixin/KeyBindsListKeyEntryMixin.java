package dev.nifte.mixin;

import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.config.KeybindConflicts;

@Mixin(KeyBindsList.KeyEntry.class)
public abstract class KeyBindsListKeyEntryMixin {
	@Shadow
	@Final
	private KeyMapping key;

	@Shadow
	@Final
	private Button changeButton;

	@Shadow
	@Final
	private Button resetButton;

	@Shadow
	private boolean hasCollision;

	@Inject(method = "refreshEntry", at = @At("HEAD"), cancellable = true)
	private void nifte$showAllConflicts(CallbackInfo ci) {
		this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
		this.resetButton.active = !this.key.isDefault();
		List<KeyMapping> conflicts = KeybindConflicts.of(this.key);
		this.hasCollision = !conflicts.isEmpty();
		if (this.hasCollision) {
			this.changeButton.setMessage(KeybindConflicts.withConflictBrackets(this.changeButton.getMessage()));
			this.changeButton.setTooltip(Tooltip.create(KeybindConflicts.tooltip(conflicts)));
		} else {
			this.changeButton.setTooltip(null);
		}

		if (Minecraft.getInstance().gui.screen() instanceof KeyBindsScreen screen && screen.selectedKey == this.key) {
			this.changeButton.setMessage(KeybindConflicts.withSelectionBrackets(this.changeButton.getMessage()));
		}

		ci.cancel();
	}
}
