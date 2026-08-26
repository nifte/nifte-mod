package dev.nifte.feature.particles;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import dev.nifte.config.NifteConfig;

public final class ParticleFilter {
	private ParticleFilter() {
	}

	public static boolean isDisabled(ParticleType<?> type) {
		Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(type);
		return id != null && NifteConfig.get().isParticleDisabled(id);
	}
}
