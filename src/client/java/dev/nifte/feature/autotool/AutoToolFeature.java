package dev.nifte.feature.autotool;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import dev.nifte.inventory.InventoryClicks;

public final class AutoToolFeature {
	private AutoToolFeature() {
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
		AutoToolScore bestScore = score(inventory.getItem(currentSlot), state, minecraft.level);
		int bestSlot = currentSlot;
		for (int slot = 0; slot < slotCount; slot++) {
			if (slot == currentSlot) {
				continue;
			}

			AutoToolScore candidate = score(inventory.getItem(slot), state, minecraft.level);
			if (candidate.isBetterThan(bestScore)) {
				bestScore = candidate;
				bestSlot = slot;
			}
		}

		if (bestSlot == currentSlot) {
			return;
		}

		if (bestSlot < Inventory.SELECTION_SIZE) {
			inventory.setSelectedSlot(bestSlot);
			player.connection.send(new ServerboundSetCarriedItemPacket(bestSlot));
			return;
		}

		if (player.containerMenu != player.inventoryMenu || !InventoryClicks.cursorEmpty()) {
			return;
		}

		int menuSlot = InventoryClicks.playerMenuSlot(minecraft, bestSlot);
		if (menuSlot >= 0) {
			InventoryClicks.swapWithHotbar(menuSlot, currentSlot);
		}
	}

	private static AutoToolScore score(ItemStack stack, BlockState state, Level level) {
		ItemStack usable = ToolProtectFeature.shouldBlock(stack) ? ItemStack.EMPTY : stack;
		boolean canHarvest = !state.requiresCorrectToolForDrops() || usable.isCorrectToolForDrops(state);
		int enchantmentPriority = enchantmentPriority(usable, state, level);
		float speed = miningSpeed(usable, state);
		boolean healthy = !usable.nextDamageWillBreak();
		return new AutoToolScore(canHarvest, enchantmentPriority, speed, healthy);
	}

	private static float miningSpeed(ItemStack stack, BlockState state) {
		if (stack.isEmpty()) {
			return 1.0F;
		}

		float speed = stack.getDestroySpeed(state);
		if (speed > 1.0F) {
			speed += miningEfficiency(stack);
		}

		return speed;
	}

	private static float miningEfficiency(ItemStack stack) {
		final float[] total = {0.0F};
		stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
			if (attribute.equals(Attributes.MINING_EFFICIENCY) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
				total[0] += (float) modifier.amount();
			}
		});
		return total[0];
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
