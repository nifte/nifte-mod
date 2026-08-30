package dev.nifte.config;

import java.util.List;

import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;

import net.minecraft.client.gui.screens.Screen;

public final class NifteConfigPreview {
	private static NifteConfig snapshot;

	private NifteConfigPreview() {
	}

	public static boolean isOpen() {
		return snapshot != null;
	}

	public static void begin() {
		if (snapshot != null) {
			discard();
		}

		snapshot = NifteConfig.get().copy();
	}

	public static void apply(Screen screen) {
		if (snapshot == null || !(screen instanceof AbstractConfigScreen configScreen)) {
			return;
		}

		for (List<AbstractConfigEntry<?>> entries : configScreen.getCategorizedEntries().values()) {
			for (AbstractConfigEntry<?> entry : entries) {
				applyEntry(entry);
			}
		}
	}

	public static void commit() {
		if (snapshot == null) {
			return;
		}

		snapshot = null;
		NifteConfig.write();
	}

	public static void discard() {
		if (snapshot == null) {
			return;
		}

		String tab = NifteConfig.get().lastConfigCategory;
		boolean tabChanged = !tab.equals(snapshot.lastConfigCategory);
		NifteConfig.restore(snapshot);
		NifteConfig.get().lastConfigCategory = tab;
		snapshot = null;
		if (tabChanged) {
			NifteConfig.write();
		}
	}

	private static void applyEntry(AbstractConfigEntry<?> entry) {
		if (entry instanceof SubCategoryListEntry category) {
			for (AbstractConfigEntry<?> child : category.getValue()) {
				applyEntry(child);
			}
			return;
		}

		if (entry.getError().isPresent()) {
			return;
		}

		entry.save();
	}
}
