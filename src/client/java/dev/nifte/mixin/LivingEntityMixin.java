package dev.nifte.mixin;

import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.particles.FirstPersonEffectParticles;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(
		method = "tickEffects",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
		),
		cancellable = true
	)
	private void nifte$hideFirstPersonEffectParticles(CallbackInfo ci) {
		if (FirstPersonEffectParticles.shouldHide((LivingEntity) (Object) this)) {
			ci.cancel();
		}
	}
}
