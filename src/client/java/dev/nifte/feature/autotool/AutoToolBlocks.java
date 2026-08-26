package dev.nifte.feature.autotool;

import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;

final class AutoToolBlocks {
	private AutoToolBlocks() {
	}

	static boolean prefersSilkTouch(BlockState state) {
		if (state.is(BlockTags.IMPERMEABLE)
			|| state.is(BlockTags.ICE)
			|| state.is(BlockTags.LEAVES)
			|| state.is(BlockTags.CORAL_BLOCKS)
			|| state.is(BlockTags.CORALS)
			|| state.is(BlockTags.WALL_CORALS)
			|| state.is(BlockTags.CORAL_PLANTS)
			|| state.is(BlockTags.BEEHIVES)
			|| state.is(BlockTags.SNOW)
			|| state.is(BlockTags.NYLIUM)
			|| state.is(BlockTags.GRASS_BLOCKS)
			|| state.is(BlockTags.CAMPFIRES)) {
			return true;
		}

		return state.getBlock() instanceof InfestedBlock
			|| state.is(Blocks.GLOWSTONE)
			|| state.is(Blocks.SEA_LANTERN)
			|| state.is(Blocks.ENDER_CHEST)
			|| state.is(Blocks.BOOKSHELF)
			|| state.is(Blocks.CLAY)
			|| state.is(Blocks.PODZOL)
			|| state.is(Blocks.MYCELIUM)
			|| state.is(Blocks.DIRT_PATH)
			|| state.is(Blocks.AMETHYST_CLUSTER)
			|| state.is(Blocks.LARGE_AMETHYST_BUD)
			|| state.is(Blocks.MEDIUM_AMETHYST_BUD)
			|| state.is(Blocks.SMALL_AMETHYST_BUD)
			|| state.is(Blocks.SCULK)
			|| state.is(Blocks.SCULK_VEIN)
			|| state.is(Blocks.SCULK_SENSOR)
			|| state.is(Blocks.CALIBRATED_SCULK_SENSOR)
			|| state.is(Blocks.SCULK_CATALYST)
			|| state.is(Blocks.SCULK_SHRIEKER)
			|| state.is(Blocks.BROWN_MUSHROOM_BLOCK)
			|| state.is(Blocks.RED_MUSHROOM_BLOCK)
			|| state.is(Blocks.MUSHROOM_STEM)
			|| state.is(Blocks.TURTLE_EGG)
			|| state.is(Blocks.DECORATED_POT);
	}

	static boolean prefersFortune(BlockState state) {
		if (prefersSilkTouch(state)) {
			return false;
		}

		return isOre(state)
			|| state.is(BlockTags.CROPS)
			|| state.is(BlockTags.CAVE_VINES)
			|| state.is(Blocks.NETHER_QUARTZ_ORE)
			|| state.is(Blocks.GILDED_BLACKSTONE)
			|| state.is(Blocks.GRAVEL)
			|| state.is(Blocks.MELON)
			|| state.is(Blocks.NETHER_WART)
			|| state.is(Blocks.COCOA)
			|| state.is(Blocks.SWEET_BERRY_BUSH);
	}

	private static boolean isOre(BlockState state) {
		return state.is(BlockItemTags.COAL_ORES.block())
			|| state.is(BlockItemTags.COPPER_ORES.block())
			|| state.is(BlockItemTags.DIAMOND_ORES.block())
			|| state.is(BlockItemTags.EMERALD_ORES.block())
			|| state.is(BlockItemTags.GOLD_ORES.block())
			|| state.is(BlockItemTags.IRON_ORES.block())
			|| state.is(BlockItemTags.LAPIS_ORES.block())
			|| state.is(BlockItemTags.REDSTONE_ORES.block());
	}
}
