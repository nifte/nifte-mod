package dev.nifte.feature.autotool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;

import org.jspecify.annotations.Nullable;

import dev.nifte.Nifte;

public final class SilkTouchBlocks {
	private static List<Block> blocks = List.of();
	private static Set<Block> affected = Set.of();
	private static boolean discovered;

	private SilkTouchBlocks() {
	}

	public static List<Block> blocks() {
		discover();
		return blocks;
	}

	public static boolean defaultsToPrefer(Block block) {
		return AutoToolBlocks.defaultsToSilk(block.defaultBlockState());
	}

	static boolean worksOn(Block block) {
		discover();
		return affected.contains(block);
	}

	private static void discover() {
		if (discovered) {
			return;
		}

		Map<Identifier, Boolean> silkTables = new HashMap<>();
		List<Block> found = new ArrayList<>();
		PackResources vanilla = ServerPacksSource.createVanillaPackSource().fullResources();
		for (Block block : BuiltInRegistries.BLOCK) {
			if (block == Blocks.DECORATED_POT || hasSilkTouchLoot(block, vanilla, silkTables)) {
				found.add(block);
			}
		}

		found.sort(Comparator.comparing((Block block) -> block.getName().getString(), String.CASE_INSENSITIVE_ORDER)
			.thenComparing(SilkTouchBlocks::id));
		blocks = List.copyOf(found);
		affected = new HashSet<>(found);
		discovered = true;
		if (!affected.contains(Blocks.GLASS)) {
			Nifte.LOGGER.warn("Silk Touch preferences could not read block loot tables");
		}
	}

	private static boolean hasSilkTouchLoot(Block block, PackResources vanilla, Map<Identifier, Boolean> silkTables) {
		Optional<ResourceKey<LootTable>> lootTable = block.getLootTable();
		if (lootTable.isEmpty()) {
			return false;
		}

		return silkTables.computeIfAbsent(lootTable.get().identifier(), id -> referencesSilkTouch(vanilla, id));
	}

	private static boolean referencesSilkTouch(PackResources vanilla, Identifier tableId) {
		try (InputStream stream = openLootTable(vanilla, tableId)) {
			if (stream == null) {
				return false;
			}

			return new String(stream.readAllBytes(), StandardCharsets.UTF_8).contains("silk_touch");
		} catch (IOException exception) {
			return false;
		}
	}

	private static @Nullable InputStream openLootTable(PackResources vanilla, Identifier tableId) throws IOException {
		String classpath = "data/" + tableId.getNamespace() + "/loot_table/" + tableId.getPath() + ".json";
		InputStream stream = SilkTouchBlocks.class.getClassLoader().getResourceAsStream(classpath);
		if (stream != null) {
			return stream;
		}

		Identifier resourceId = Identifier.fromNamespaceAndPath(tableId.getNamespace(), "loot_table/" + tableId.getPath() + ".json");
		IoSupplier<InputStream> supplier = vanilla.getResource(PackType.SERVER_DATA, resourceId);
		return supplier == null ? null : supplier.get();
	}

	private static String id(Block block) {
		Identifier id = BuiltInRegistries.BLOCK.getKey(block);
		return id == null ? "" : id.toString();
	}
}
