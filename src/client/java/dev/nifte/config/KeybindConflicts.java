package dev.nifte.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import dev.nifte.mixin.KeyMappingAccessor;

public final class KeybindConflicts {
	private KeybindConflicts() {
	}

	public static List<KeyMapping> of(KeyMapping mapping) {
		if (mapping == null || mapping.isUnbound()) {
			return List.of();
		}

		List<KeyMapping> conflicts = new ArrayList<>();
		for (KeyMapping other : allMappings()) {
			if (other == mapping || !mapping.same(other) || skipDebugPair(mapping, other)) {
				continue;
			}

			conflicts.add(other);
		}

		return conflicts;
	}

	public static Component tooltip(List<KeyMapping> conflicts) {
		MutableComponent names = Component.empty();
		for (int index = 0; index < conflicts.size(); index++) {
			if (index > 0) {
				names.append(", ");
			}

			names.append(Component.translatable(conflicts.get(index).getName()));
		}

		return Component.translatable("controls.keybinds.duplicateKeybinds", names);
	}

	public static Component withConflictBrackets(Component message) {
		return Component.literal("[ ")
			.append(message.copy().withStyle(ChatFormatting.WHITE))
			.append(" ]")
			.withStyle(ChatFormatting.YELLOW);
	}

	public static Component withSelectionBrackets(Component message) {
		return Component.literal("> ")
			.append(message.copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
			.append(" <")
			.withStyle(ChatFormatting.YELLOW);
	}

	private static Set<KeyMapping> allMappings() {
		Set<KeyMapping> mappings = new LinkedHashSet<>(KeyMappingAccessor.nifte$all().values());
		KeyMapping[] optionKeys = Minecraft.getInstance().options.keyMappings;
		if (optionKeys != null) {
			Collections.addAll(mappings, optionKeys);
		}

		return mappings;
	}

	private static boolean skipDebugPair(KeyMapping left, KeyMapping right) {
		return left.getCategory() == KeyMapping.Category.DEBUG && right.getCategory() == KeyMapping.Category.DEBUG;
	}
}
