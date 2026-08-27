package dev.nifte.config;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class ConfigKeybindEditor {
	private static final int BUTTON_MIN_WIDTH = 70;
	private static final int BUTTON_MAX_WIDTH = 120;
	private static final int BUTTON_PADDING = 12;

	private static KeyMapping selected;

	private ConfigKeybindEditor() {
	}

	public static void clear() {
		selected = null;
	}

	public static boolean isSelected(KeyMapping mapping) {
		return selected == mapping;
	}

	public static Button createButton(KeyMapping mapping) {
		return Button.builder(Component.empty(), button -> selected = mapping)
			.bounds(0, 0, BUTTON_MIN_WIDTH, 20)
			.build();
	}

	public static void layout(Button button, KeyMapping mapping, int y, int right, boolean visible, boolean active) {
		button.visible = visible;
		button.active = visible && active;
		if (!visible) {
			if (isSelected(mapping)) {
				clear();
			}

			return;
		}

		update(button, mapping);
		button.setY(y);
		button.setWidth(width(button));
		button.setX(right - button.getWidth());
	}

	public static void update(Button button, KeyMapping mapping) {
		List<KeyMapping> conflicts = KeybindConflicts.of(mapping);
		button.setMessage(message(mapping, conflicts));
		if (conflicts.isEmpty()) {
			button.setTooltip(null);
			return;
		}

		button.setTooltip(Tooltip.create(KeybindConflicts.tooltip(conflicts)));
	}

	public static boolean isOver(Button button, double mouseX, double mouseY) {
		return button.visible && button.isMouseOver(mouseX, mouseY);
	}

	public static boolean keyPressed(KeyEvent event) {
		if (!capturing()) {
			return false;
		}

		if (event.isEscape()) {
			bind(InputConstants.UNKNOWN);
		} else {
			bind(InputConstants.getKey(event));
		}

		return true;
	}

	public static boolean mouseClicked(MouseButtonEvent event) {
		if (!capturing()) {
			return false;
		}

		bind(InputConstants.Type.MOUSE.getOrCreate(event.button()));
		return true;
	}

	private static boolean capturing() {
		if (selected == null) {
			return false;
		}

		if (!NifteConfigScreen.isNifteScreen(Minecraft.getInstance().gui.screen())) {
			clear();
			return false;
		}

		return true;
	}

	private static void bind(InputConstants.Key key) {
		selected.setKey(key);
		KeyMapping.resetMapping();
		Minecraft.getInstance().options.save();
		clear();
	}

	private static Component message(KeyMapping mapping, List<KeyMapping> conflicts) {
		Component label = mapping.getTranslatedKeyMessage();
		if (!conflicts.isEmpty()) {
			label = KeybindConflicts.withConflictBrackets(label);
		}

		if (isSelected(mapping)) {
			return KeybindConflicts.withSelectionBrackets(label);
		}

		return label;
	}

	private static int width(Button button) {
		return Mth.clamp(
			Minecraft.getInstance().font.width(button.getMessage()) + BUTTON_PADDING,
			BUTTON_MIN_WIDTH,
			BUTTON_MAX_WIDTH
		);
	}
}
