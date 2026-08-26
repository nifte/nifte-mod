package dev.nifte.feature.shulker;

import java.util.ArrayList;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

import dev.nifte.config.NifteConfig;

public final class ShulkerBoxTooltips {
	private ShulkerBoxTooltips() {
	}

	public static Optional<TooltipComponent> image(ItemStack stack) {
		if (!enabledFor(stack)) {
			return Optional.empty();
		}

		ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		if (!hasItems(contents)) {
			return Optional.empty();
		}

		NonNullList<ItemStack> items = NonNullList.withSize(ShulkerBoxBlockEntity.CONTAINER_SIZE, ItemStack.EMPTY);
		contents.copyInto(items);
		return Optional.of(new ShulkerBoxTooltip(new ArrayList<>(items)));
	}

	public static boolean hidesVanillaList(ItemStack stack) {
		if (!enabledFor(stack)) {
			return false;
		}

		if (!stack.has(DataComponents.CONTAINER_LOOT)) {
			return true;
		}

		return hasItems(stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
	}

	public static Optional<Component> emptyLine(ItemStack stack) {
		if (!enabledFor(stack) || stack.has(DataComponents.CONTAINER_LOOT)) {
			return Optional.empty();
		}

		ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		if (hasItems(contents)) {
			return Optional.empty();
		}

		return Optional.of(Component.translatable("nifte.shulker.empty").withStyle(ChatFormatting.GRAY));
	}

	private static boolean enabledFor(ItemStack stack) {
		return NifteConfig.get().shulkerBoxTooltipEnabled && !stack.isEmpty() && stack.is(ItemTags.SHULKER_BOXES);
	}

	private static boolean hasItems(ItemContainerContents contents) {
		return contents.nonEmptyItems().iterator().hasNext();
	}
}
