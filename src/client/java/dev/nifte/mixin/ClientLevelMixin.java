package dev.nifte.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.particles.ParticleFilter;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
	@Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
	private void nifte$filterDestroyParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (ParticleFilter.isDisabled(ParticleTypes.BLOCK)) {
			ci.cancel();
		}
	}

	@Inject(method = "addBreakingBlockEffect", at = @At("HEAD"), cancellable = true)
	private void nifte$filterBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
		if (ParticleFilter.isDisabled(ParticleTypes.BLOCK)) {
			ci.cancel();
		}
	}
}
