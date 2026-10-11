package dev.nifte.feature.shiftdrop;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

import dev.nifte.config.NifteConfig;
import dev.nifte.mixin.AbstractContainerScreenAccessor;

public final class ShiftDropFeature {
	// Throw uses button 1 for the whole stack, the same click control-drop sends.
	private static final int WHOLE_STACK = 1;

	private ShiftDropFeature() {
	}

	public static boolean handleKey(AbstractContainerScreen<?> screen, KeyEvent event) {
		if (!NifteConfig.get().shiftDropStackEnabled || !event.hasShiftDown() || screen.getFocused() instanceof EditBox) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || !minecraft.options.keyDrop.matches(event)) {
			return false;
		}

		Slot hovered = ((AbstractContainerScreenAccessor) screen).nifte$hoveredSlot();
		if (hovered == null || !hovered.hasItem()) {
			return false;
		}

		((AbstractContainerScreenAccessor) screen).nifte$slotClicked(hovered, hovered.index, WHOLE_STACK, ContainerInput.THROW);
		return true;
	}
}
