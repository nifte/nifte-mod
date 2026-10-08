package dev.nifte.mixin;

import java.util.concurrent.atomic.AtomicInteger;

import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = IntegerSliderEntry.class, remap = false)
public interface IntegerSliderEntryAccessor {
	@Accessor("minimum")
	int nifte$minimum();

	@Accessor("maximum")
	int nifte$maximum();

	@Accessor("value")
	AtomicInteger nifte$value();
}
