package dev.nifte.config;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;

public final class ConfigEntryControls {
	private ConfigEntryControls() {
	}

	public static boolean isLeftOfControls(Iterable<? extends GuiEventListener> children, double mouseX) {
		int controlLeft = Integer.MAX_VALUE;
		for (GuiEventListener child : children) {
			if (child instanceof AbstractWidget widget && widget.visible) {
				controlLeft = Math.min(controlLeft, widget.getX());
			}
		}

		return mouseX < controlLeft;
	}
}
