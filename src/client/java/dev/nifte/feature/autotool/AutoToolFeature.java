package dev.nifte.feature.autotool;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;
import dev.nifte.feature.toolprotect.ToolProtectFeature;
import dev.nifte.hud.ToggleOverlay;

public final class AutoToolFeature {
	private AutoToolFeature() {
	}

	public static void register() {
		AutoToolSwitchBack.register();
	}

	public static void toggle() {
		NifteConfig config = NifteConfig.get();
		config.autoToolEnabled = !config.autoToolEnabled;
		NifteConfig.save();

		ToggleOverlay.show("nifte.auto_tool.toggle", config.autoToolEnabled);
	}

	public static void selectFor(Minecraft minecraft, BlockPos pos) {
		NifteConfig config = NifteConfig.get();
		if (!config.autoToolEnabled || minecraft.player == null || minecraft.level == null || minecraft.gui.screen() != null) {
			return;
		}

		LocalPlayer player = minecraft.player;
		if (player.getAbilities().instabuild || player.isSpectator()) {
			return;
		}

		BlockState state = minecraft.level.getBlockState(pos);
		if (state.isAir() || state.getDestroySpeed(minecraft.level, pos) < 0.0F) {
			return;
		}

		Inventory inventory = player.getInventory();
		int currentSlot = inventory.getSelectedSlot();
		int slotCount = config.autoToolFromInventory ? Inventory.INVENTORY_SIZE : Inventory.SELECTION_SIZE;
		int bestSlot = AutoToolSlots.bestSlot(inventory, currentSlot, slotCount, stack -> score(stack, state, minecraft.level));
		boolean fromInventory = bestSlot >= Inventory.SELECTION_SIZE;
		ItemStack tool = ItemStack.EMPTY;
		ItemStack displaced = ItemStack.EMPTY;
		if (config.autoToolSwitchBack && fromInventory && bestSlot != currentSlot) {
			tool = inventory.getItem(bestSlot).copy();
			displaced = inventory.getItem(currentSlot).copy();
		}

		if (AutoToolSlots.swap(minecraft, player, bestSlot, currentSlot) && config.autoToolSwitchBack) {
			AutoToolSwitchBack.record(currentSlot, bestSlot, tool, displaced);
		}
	}

	public static void selectFor(Minecraft minecraft, Entity target) {
		AutoToolEntities.selectFor(minecraft, target);
	}

	private static AutoToolScore score(ItemStack stack, BlockState state, Level level) {
		ItemStack usable = ToolProtectFeature.shouldBlock(stack) ? ItemStack.EMPTY : stack;
		boolean canHarvest = !state.requiresCorrectToolForDrops() || usable.isCorrectToolForDrops(state);
		int enchantmentPriority = enchantmentPriority(usable, state, level);
		boolean preferredTool = AutoToolSpeed.prefersTool(usable, state);
		float speed = AutoToolSpeed.miningSpeed(usable, state);
		boolean matchesTool = AutoToolSpeed.matchesMineableTag(usable, state);
		return new AutoToolScore(canHarvest, enchantmentPriority, preferredTool, speed, matchesTool);
	}

	private static int enchantmentPriority(ItemStack stack, BlockState state, Level level) {
		if (stack.isEmpty()) {
			return 0;
		}

		if (AutoToolBlocks.prefersSilkTouch(state)) {
			return enchantmentLevel(stack, level, Enchantments.SILK_TOUCH) > 0 ? 1 : 0;
		}

		if (AutoToolBlocks.prefersFortune(state)) {
			return enchantmentLevel(stack, level, Enchantments.FORTUNE);
		}

		return 0;
	}

	private static int enchantmentLevel(ItemStack stack, Level level, ResourceKey<Enchantment> enchantment) {
		Holder<Enchantment> holder = holder(level, enchantment);
		if (holder == null) {
			return 0;
		}

		return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
	}

	private static @Nullable Holder<Enchantment> holder(Level level, ResourceKey<Enchantment> enchantment) {
		return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(enchantment).orElse(null);
	}
}
