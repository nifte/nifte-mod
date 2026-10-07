package dev.nifte.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import org.jspecify.annotations.Nullable;

import dev.nifte.mixin.ColorParticleOptionAccessor;
import dev.nifte.mixin.LivingEntityEffectParticlesAccessor;

final class MobEffectParticles {
	private static final Map<Integer, Holder<MobEffect>> BY_COLOR = new HashMap<>();
	private static final Map<ParticleType<?>, Holder<MobEffect>> BY_PARTICLE = new HashMap<>();
	private static boolean indexed;

	private MobEffectParticles() {
	}

	static List<MobEffectInstance> visible(LivingEntity entity) {
		List<ParticleOptions> particles = entity.getEntityData().get(LivingEntityEffectParticlesAccessor.nifte$effectParticles());
		if (particles.isEmpty()) {
			return List.of();
		}

		index();
		boolean ambient = entity.getEntityData().get(LivingEntityEffectParticlesAccessor.nifte$effectAmbience());
		List<MobEffectInstance> effects = new ArrayList<>();
		Set<Holder<MobEffect>> seen = new HashSet<>();
		for (ParticleOptions particle : particles) {
			Holder<MobEffect> effect = effect(particle);
			if (effect != null && seen.add(effect)) {
				effects.add(new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, 0, ambient, true, true));
			}
		}

		effects.sort(Comparator.reverseOrder());
		return effects;
	}

	private static @Nullable Holder<MobEffect> effect(ParticleOptions particle) {
		if (particle instanceof ColorParticleOption color) {
			return BY_COLOR.get(((ColorParticleOptionAccessor) color).nifte$color() & 0xFFFFFF);
		}

		return BY_PARTICLE.get(particle.getType());
	}

	private static void index() {
		if (indexed) {
			return;
		}

		BuiltInRegistries.MOB_EFFECT.forEach(effect -> {
			Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
			ParticleOptions particle = effect.createParticleOptions(new MobEffectInstance(holder, 1));
			if (particle instanceof ColorParticleOption color) {
				BY_COLOR.putIfAbsent(((ColorParticleOptionAccessor) color).nifte$color() & 0xFFFFFF, holder);
			} else {
				BY_PARTICLE.putIfAbsent(particle.getType(), holder);
			}
		});
		indexed = true;
	}
}
