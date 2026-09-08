package dev.nifte.feature.particles;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ParticleTooltips {
	public static final String FIRST_PERSON_ENTITY_EFFECT_LABEL = "entity_effect (first person)";

	private static final Identifier ENTITY_EFFECT = Identifier.withDefaultNamespace("entity_effect");

	private ParticleTooltips() {
	}

	public static boolean isEntityEffect(Identifier id) {
		return ENTITY_EFFECT.equals(id);
	}

	public static Component describe(Identifier id) {
		String fallback = "minecraft".equals(id.getNamespace())
			? "Vanilla particle: " + id.getPath() + "."
			: "Particle from " + id + ".";
		return Component.translatableWithFallback(key(id), fallback);
	}

	public static Component describeFirstPersonEntityEffect() {
		return Component.translatable("nifte.config.particles.tooltip.entity_effect.first_person");
	}

	private static String key(Identifier id) {
		if ("minecraft".equals(id.getNamespace())) {
			return "nifte.config.particles.tooltip." + id.getPath();
		}

		return "nifte.config.particles.tooltip." + id.getNamespace() + "." + id.getPath();
	}
}
