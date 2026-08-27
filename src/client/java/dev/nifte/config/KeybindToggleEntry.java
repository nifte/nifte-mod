package dev.nifte.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public final class KeybindToggleEntry extends BooleanListEntry {
	private static final int KEY_BUTTON_GAP = 2;

	private final KeyMapping mapping;
	private final Button keyButton;
	private final Function<Boolean, Component> yesNoText;

	public KeybindToggleEntry(
		Component fieldName,
		boolean value,
		Component resetButtonKey,
		Supplier<Boolean> defaultValue,
		Consumer<Boolean> saveConsumer,
		KeyMapping mapping,
		Function<Boolean, Component> yesNoText
	) {
		super(fieldName, value, resetButtonKey, defaultValue, saveConsumer, null, false);
		this.mapping = mapping;
		this.yesNoText = yesNoText;
		this.keyButton = ConfigKeybindEditor.createButton(mapping);
	}

	@Override
	public Component getYesNoText(boolean value) {
		return this.yesNoText.apply(value);
	}

	@Override
	public Optional<Component[]> getTooltip(int mouseX, int mouseY) {
		if (ConfigKeybindEditor.isOver(this.keyButton, mouseX, mouseY)) {
			return Optional.empty();
		}

		return super.getTooltip(mouseX, mouseY);
	}

	@Override
	public void extractRenderState(
		GuiGraphicsExtractor graphics,
		int index,
		int y,
		int x,
		int entryWidth,
		int entryHeight,
		int mouseX,
		int mouseY,
		boolean isHovered,
		float delta
	) {
		super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
		Button toggle = toggleButton();
		ConfigKeybindEditor.layout(
			this.keyButton,
			this.mapping,
			toggle.getY(),
			toggle.getX() - KEY_BUTTON_GAP,
			true,
			isEditable()
		);
		this.keyButton.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		List<GuiEventListener> widgets = new ArrayList<>();
		widgets.add(this.keyButton);
		widgets.addAll(super.children());
		return widgets;
	}

	@Override
	public List<? extends NarratableEntry> narratables() {
		List<NarratableEntry> widgets = new ArrayList<>();
		widgets.add(this.keyButton);
		for (GuiEventListener child : super.children()) {
			if (child instanceof NarratableEntry narratable) {
				widgets.add(narratable);
			}
		}

		return widgets;
	}

	private Button toggleButton() {
		return (Button) super.children().get(0);
	}
}
