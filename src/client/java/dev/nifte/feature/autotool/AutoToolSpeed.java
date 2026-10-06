package dev.nifte.feature.autotool;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class AutoToolSpeed {
	private AutoToolSpeed() {
	}

	static float miningSpeed(ItemStack stack, BlockState state) {
		if (stack.isEmpty()) {
			return 1.0F;
		}

		float speed = stack.getDestroySpeed(state);
		if (speed <= 1.0F && matchesMineableTag(stack, state)) {
			speed = Math.max(speed, fastestRuleSpeed(stack));
		}

		if (speed > 1.0F) {
			speed += miningEfficiency(stack);
		}

		return speed;
	}

	static boolean prefersTool(ItemStack stack, BlockState state) {
		return isHoe(stack) && isWartBlock(state);
	}

	static boolean matchesMineableTag(ItemStack stack, BlockState state) {
		if (stack.isEmpty()) {
			return false;
		}

		return (isHoe(stack) && (state.is(BlockTags.MINEABLE_WITH_HOE) || isWartBlock(state)))
			|| (stack.is(ItemTags.AXES) && state.is(BlockTags.MINEABLE_WITH_AXE))
			|| (stack.is(ItemTags.PICKAXES) && state.is(BlockTags.MINEABLE_WITH_PICKAXE))
			|| (stack.is(ItemTags.SHOVELS) && state.is(BlockTags.MINEABLE_WITH_SHOVEL));
	}

	private static boolean isHoe(ItemStack stack) {
		return stack.is(ItemTags.HOES)
			|| stack.is(Items.WOODEN_HOE)
			|| stack.is(Items.STONE_HOE)
			|| stack.is(Items.COPPER_HOE)
			|| stack.is(Items.IRON_HOE)
			|| stack.is(Items.GOLDEN_HOE)
			|| stack.is(Items.DIAMOND_HOE)
			|| stack.is(Items.NETHERITE_HOE);
	}

	private static boolean isWartBlock(BlockState state) {
		return state.is(BlockTags.WART_BLOCKS) || state.is(Blocks.NETHER_WART_BLOCK) || state.is(Blocks.WARPED_WART_BLOCK);
	}

	private static float fastestRuleSpeed(ItemStack stack) {
		Tool tool = stack.get(DataComponents.TOOL);
		if (tool == null) {
			return 1.0F;
		}

		float fastest = 1.0F;
		for (Tool.Rule rule : tool.rules()) {
			if (rule.speed().isPresent()) {
				fastest = Math.max(fastest, rule.speed().get());
			}
		}

		return fastest;
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
}
