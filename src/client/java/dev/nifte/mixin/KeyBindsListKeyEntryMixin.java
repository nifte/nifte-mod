package dev.nifte.mixin;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
		this.hasCollision = false;
		MutableComponent tooltip = Component.empty();
		if (!this.key.isUnbound()) {
			for (KeyMapping otherKey : nifte$allMappings()) {
				if (otherKey == this.key || !this.key.same(otherKey) || nifte$skipDebugPair(this.key, otherKey)) {
					continue;
				}
				if (this.hasCollision) {
					tooltip.append(", ");
				}
				this.hasCollision = true;
				tooltip.append(Component.translatable(otherKey.getName()));
			}
		}
		if (this.hasCollision) {
			this.changeButton.setMessage(
				Component.literal("[ ").append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE)).append(" ]").withStyle(ChatFormatting.YELLOW)
			);
			this.changeButton.setTooltip(Tooltip.create(Component.translatable("controls.keybinds.duplicateKeybinds", tooltip)));
		} else {
			this.changeButton.setTooltip(null);
		}
		if (Minecraft.getInstance().gui.screen() instanceof KeyBindsScreen screen && screen.selectedKey == this.key) {
			this.changeButton.setMessage(
				Component.literal("> ")
					.append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
					.append(" <")
					.withStyle(ChatFormatting.YELLOW)
			);
		}
		ci.cancel();
	}

	@Unique
	private static Set<KeyMapping> nifte$allMappings() {
		Set<KeyMapping> mappings = new LinkedHashSet<>(KeyMappingAccessor.nifte$all().values());
		KeyMapping[] optionKeys = Minecraft.getInstance().options.keyMappings;
		if (optionKeys != null) {
			Collections.addAll(mappings, optionKeys);
		}
		return mappings;
	}

	@Unique
	private static boolean nifte$skipDebugPair(KeyMapping left, KeyMapping right) {
		return left.getCategory() == KeyMapping.Category.DEBUG && right.getCategory() == KeyMapping.Category.DEBUG;
	}
}
