package dev.nifte.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Accessor("hoveredSlot")
	Slot nifte$hoveredSlot();

	@Invoker("getHoveredSlot")
	Slot nifte$getHoveredSlot(double x, double y);

	@Accessor("leftPos")
	int nifte$leftPos();

	@Accessor("topPos")
	int nifte$topPos();

	@Invoker("hasClickedOutside")
	boolean nifte$hasClickedOutside(double mouseX, double mouseY, int left, int top);
}
