package dev.nifte.feature.food;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

import dev.nifte.config.NifteConfig;
import dev.nifte.input.NifteKeybinds;

public final class QuickEatFeature {
	private static boolean active;
	private static int originalSlot = -1;

	private QuickEatFeature() {
	}

	public static boolean shouldKeepUsing() {
		if (!active || !enabled() || !keyHeld()) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		return player != null && player.isUsingItem() && isFood(player.getUseItem());
	}

	public static void tick(Minecraft minecraft) {
		if (!canRun(minecraft)) {
			stop(minecraft);
			return;
		}

		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.gameMode == null) {
			stop(minecraft);
			return;
		}

		if (player.isUsingItem()) {
			return;
		}

		if (!player.getFoodData().needsFood()) {
			stop(minecraft);
			return;
		}

		int foodSlot = findBestFoodSlot(player);
		if (foodSlot < 0) {
			stop(minecraft);
			return;
		}

		begin(player);
		selectSlot(player, foodSlot);
		minecraft.gameMode.useItem(player, InteractionHand.MAIN_HAND);
	}

	private static boolean canRun(Minecraft minecraft) {
		if (!enabled() || !keyHeld() || minecraft.isPaused() || minecraft.gui.screen() != null) {
			return false;
		}

		LocalPlayer player = minecraft.player;
		return player != null
			&& minecraft.gameMode != null
			&& player.isAlive()
			&& !player.isSpectator()
			&& !player.hasInfiniteMaterials()
			&& !player.isHandsBusy()
			&& !minecraft.gameMode.isDestroying();
	}

	private static boolean enabled() {
		return NifteConfig.get().quickEatEnabled;
	}

	private static boolean keyHeld() {
		return NifteKeybinds.quickEat != null && NifteKeybinds.quickEat.isDown();
	}

	private static void begin(LocalPlayer player) {
		if (active) {
			return;
		}

		active = true;
		originalSlot = player.getInventory().getSelectedSlot();
	}

	private static void stop(Minecraft minecraft) {
		if (!active) {
			return;
		}

		LocalPlayer player = minecraft.player;
		if (player != null && minecraft.gameMode != null && player.isUsingItem() && isFood(player.getUseItem())) {
			minecraft.gameMode.releaseUsingItem(player);
		}

		if (player != null && originalSlot >= 0 && originalSlot < Inventory.SELECTION_SIZE) {
			selectSlot(player, originalSlot);
		}

		active = false;
		originalSlot = -1;
	}

	private static void selectSlot(LocalPlayer player, int slot) {
		Inventory inventory = player.getInventory();
		if (inventory.getSelectedSlot() == slot) {
			return;
		}

		inventory.setSelectedSlot(slot);
		player.connection.send(new ServerboundSetCarriedItemPacket(slot));
	}

	private static int findBestFoodSlot(LocalPlayer player) {
		Inventory inventory = player.getInventory();
		int selected = inventory.getSelectedSlot();
		int bestSlot = -1;
		FoodScore bestScore = null;
		for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!canEat(player, stack)) {
				continue;
			}

			FoodScore score = score(stack);
			if (bestScore == null || score.isBetterThan(bestScore) || (score.equals(bestScore) && slot == selected)) {
				bestScore = score;
				bestSlot = slot;
			}
		}

		return bestSlot;
	}

	private static boolean canEat(LocalPlayer player, ItemStack stack) {
		FoodProperties food = stack.get(DataComponents.FOOD);
		if (food == null || player.getCooldowns().isOnCooldown(stack)) {
			return false;
		}

		return player.canEat(food.canAlwaysEat());
	}

	private static boolean isFood(ItemStack stack) {
		return stack.get(DataComponents.FOOD) != null;
	}

	private static FoodScore score(ItemStack stack) {
		FoodProperties food = stack.get(DataComponents.FOOD);
		int nutrition = food == null ? 0 : Math.max(0, food.nutrition());
		return new FoodScore(nutrition, !makesSick(stack));
	}

	private static boolean makesSick(ItemStack stack) {
		Consumable consumable = stack.get(DataComponents.CONSUMABLE);
		if (consumable != null) {
			for (ConsumeEffect effect : consumable.onConsumeEffects()) {
				if (effect instanceof ApplyStatusEffectsConsumeEffect apply && hasHarmfulEffect(apply)) {
					return true;
				}
			}
		}

		SuspiciousStewEffects stew = stack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
		if (stew != null) {
			for (SuspiciousStewEffects.Entry entry : stew.effects()) {
				if (entry.effect().value().getCategory() == MobEffectCategory.HARMFUL) {
					return true;
				}
			}
		}

		return false;
	}

	private static boolean hasHarmfulEffect(ApplyStatusEffectsConsumeEffect apply) {
		for (MobEffectInstance instance : apply.effects()) {
			if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
				return true;
			}
		}

		return false;
	}

	private record FoodScore(int nutrition, boolean safe) implements Comparable<FoodScore> {
		boolean isBetterThan(FoodScore other) {
			return compareTo(other) > 0;
		}

		@Override
		public int compareTo(FoodScore other) {
			int hunger = Integer.compare(this.nutrition, other.nutrition);
			if (hunger != 0) {
				return hunger;
			}

			return Boolean.compare(this.safe, other.safe);
		}
	}
}
