package dev.nifte.config;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;

public final class KeybindFieldEntry extends TooltipListEntry<Boolean> {
	private final KeyMapping mapping;
	private final Button keyButton;
	private final List<AbstractWidget> widgets;

	public KeybindFieldEntry(Component fieldName, KeyMapping mapping) {
		super(fieldName, null, false);
		this.mapping = mapping;
		this.keyButton = ConfigKeybindEditor.createButton(mapping);
		this.widgets = List.of(this.keyButton);
	}

	@Override
	public Boolean getValue() {
		return Boolean.TRUE;
	}

	@Override
	public Optional<Boolean> getDefaultValue() {
		return Optional.empty();
	}

	@Override
	public boolean isEdited() {
		return false;
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
		Component name = getDisplayedFieldName();
		graphics.text(Minecraft.getInstance().font, name, x, y + 6, getPreferredTextColor(), false);
		ConfigKeybindEditor.layout(
			this.keyButton,
			this.mapping,
			y,
			x + entryWidth,
			true,
			isEditable()
		);
		this.keyButton.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return this.widgets;
	}

	@Override
	public List<? extends NarratableEntry> narratables() {
		return this.widgets;
	}
}
