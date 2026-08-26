package dev.nifte.feature.toolprotect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import dev.nifte.config.NifteConfig;

public final class ToolProtectFeature {
	private static final long NOTIFY_INTERVAL_MS = 1500L;
	private static long lastNotifyTimeMs;

	private ToolProtectFeature() {
	}

	public static boolean shouldBlock(ItemStack stack) {
		if (!NifteConfig.get().toolProtectEnabled || !isAboutToBreak(stack)) {
			return false;
		}

		LocalPlayer player = Minecraft.getInstance().player;
		return player != null && !player.getAbilities().instabuild && !player.isSpectator();
	}

	public static boolean blockMainHand(Minecraft minecraft) {
		if (minecraft.player == null || !shouldBlock(minecraft.player.getMainHandItem())) {
			return false;
		}

		notifyBlocked();
		return true;
	}

	public static boolean blockHand(LocalPlayer player, InteractionHand hand) {
		if (!shouldBlock(player.getItemInHand(hand))) {
			return false;
		}

		notifyBlocked();
		return true;
	}

	public static boolean blockEntityInteract(LocalPlayer player, Entity entity, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!shouldBlock(stack) || !damagesOnEntityInteract(stack, entity)) {
			return false;
		}

		notifyBlocked();
		return true;
	}

	public static InteractionResult useOnWhenBlocked(Minecraft minecraft, LocalPlayer player, BlockHitResult hit) {
		boolean haveSomethingInHands = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
		if (!(player.isSecondaryUseActive() && haveSomethingInHands) && minecraft.level != null) {
			BlockState state = minecraft.level.getBlockState(hit.getBlockPos());
			if (player.connection.isFeatureEnabled(state.getBlock().requiredFeatures())) {
				InteractionResult blockUse = state.useWithoutItem(minecraft.level, player, hit);
				if (blockUse.consumesAction()) {
					return blockUse;
				}
			}
		}

		notifyBlocked();
		return InteractionResult.PASS;
	}

	private static boolean isAboutToBreak(ItemStack stack) {
		if (stack.isEmpty() || !stack.isDamageableItem() || !stack.nextDamageWillBreak()) {
			return false;
		}

		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		return equippable == null || equippable.slot().getType() == EquipmentSlot.Type.HAND;
	}

	private static boolean damagesOnEntityInteract(ItemStack stack, Entity entity) {
		if (stack.is(Items.SHEARS) && entity instanceof Shearable shearable) {
			return shearable.readyForShearing();
		}

		return stack.is(Items.FLINT_AND_STEEL) && entity instanceof Creeper
			|| stack.is(Items.BRUSH);
	}

	private static void notifyBlocked() {
		long now = Util.getMillis();
		if (now - lastNotifyTimeMs < NOTIFY_INTERVAL_MS) {
			return;
		}

		lastNotifyTimeMs = now;
		Minecraft.getInstance().gui.hud.setOverlayMessage(Component.translatable("nifte.tool_protect.blocked"), false);
	}
}
