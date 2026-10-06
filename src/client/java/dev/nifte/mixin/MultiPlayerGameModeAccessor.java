package dev.nifte.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {
	@Accessor("isDestroying")
	boolean nifte$isDestroying();

	@Accessor("destroyProgress")
	float nifte$destroyProgress();

	@Accessor("destroyBlockPos")
	BlockPos nifte$destroyBlockPos();
}
