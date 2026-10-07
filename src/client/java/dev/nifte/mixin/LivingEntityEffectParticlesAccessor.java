package dev.nifte.mixin;

import java.util.List;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityEffectParticlesAccessor {
	@Accessor("DATA_EFFECT_PARTICLES")
	static EntityDataAccessor<List<ParticleOptions>> nifte$effectParticles() {
		throw new AssertionError();
	}

	@Accessor("DATA_EFFECT_AMBIENCE_ID")
	static EntityDataAccessor<Boolean> nifte$effectAmbience() {
		throw new AssertionError();
	}
}
