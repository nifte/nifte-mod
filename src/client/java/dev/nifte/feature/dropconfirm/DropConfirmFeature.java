package dev.nifte.feature.dropconfirm;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.DropConfirmMode;
import dev.nifte.config.NifteConfig;

public final class DropConfirmFeature {
	private static long lastConfirmTimeMs;

	private DropConfirmFeature() {
	}

	public static boolean shouldBlockDrop(ItemStack stack) {
		NifteConfig config = NifteConfig.get();
		if (!config.dropConfirmMode.isEnabled() || stack.isEmpty() || !matches(stack, config.dropConfirmMode)) {
			return false;
		}

		long now = Util.getMillis();
		long windowMs = Math.max(1, config.dropConfirmSeconds) * 1000L;
		if (now - lastConfirmTimeMs <= windowMs) {
			lastConfirmTimeMs = 0L;
			return false;
		}

		lastConfirmTimeMs = now;
		Component prompt = Component.translatable("nifte.drop_confirm.prompt");
		Minecraft.getInstance().gui.hud.setOverlayMessage(prompt, false);
		return true;
	}

	public static boolean handleSlotClick(AbstractContainerMenu menu, @Nullable Slot slot, int slotId, ContainerInput input) {
		return shouldBlockDrop(stackBeingDropped(menu, slot, slotId, input));
	}

	private static ItemStack stackBeingDropped(AbstractContainerMenu menu, @Nullable Slot slot, int slotId, ContainerInput input) {
		if (input == ContainerInput.THROW) {
			if (slot != null && slot.hasItem()) {
				return slot.getItem();
			}

			if (slotId == -999) {
				return menu.getCarried();
			}

			if (slotId >= 0 && slotId < menu.slots.size()) {
				return menu.slots.get(slotId).getItem();
			}

			return ItemStack.EMPTY;
		}

		if (slotId == -999 && (input == ContainerInput.PICKUP || input == ContainerInput.QUICK_MOVE)) {
			return menu.getCarried();
		}

		return ItemStack.EMPTY;
	}

	private static boolean matches(ItemStack stack, DropConfirmMode mode) {
		return switch (mode) {
			case DISABLED -> false;
			case ALL -> true;
			case ENCHANTED -> stack.isEnchanted();
			case TOOLS_AND_WEAPONS -> DropConfirmItems.isToolOrWeapon(stack);
		};
	}
}
