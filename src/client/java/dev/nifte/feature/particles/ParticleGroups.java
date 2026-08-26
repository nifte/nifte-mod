package dev.nifte.feature.particles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ParticleGroups {
	private ParticleGroups() {
	}

	public static Map<ParticleGroup, List<Identifier>> grouped() {
		Map<ParticleGroup, List<Identifier>> groups = new EnumMap<>(ParticleGroup.class);
		for (ParticleGroup group : ParticleGroup.values()) {
			groups.put(group, new ArrayList<>());
		}

		BuiltInRegistries.PARTICLE_TYPE.forEach(type -> {
			Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(type);
			if (id != null) {
				groups.get(of(id)).add(id);
			}
		});

		for (List<Identifier> ids : groups.values()) {
			ids.sort(Comparator.comparing(Identifier::toString));
		}

		return groups;
	}

	public static String label(Identifier id) {
		return "minecraft".equals(id.getNamespace()) ? id.getPath() : id.toString();
	}

	static ParticleGroup of(Identifier id) {
		if ("minecraft".equals(id.getNamespace())) {
			return vanilla(id.getPath());
		}

		return fromPath(id.getPath());
	}

	private static ParticleGroup vanilla(String path) {
		return switch (path) {
			case "crit",
				"damage_indicator",
				"enchanted_hit",
				"sweep_attack",
				"explosion",
				"explosion_emitter",
				"sonic_boom",
				"gust",
				"small_gust",
				"gust_emitter_large",
				"gust_emitter_small",
				"spit",
				"elder_guardian",
				"trail" -> ParticleGroup.COMBAT;
			case "effect",
				"instant_effect",
				"entity_effect",
				"witch",
				"enchant",
				"totem_of_undying",
				"dragon_breath",
				"note",
				"flash",
				"wax_on",
				"wax_off",
				"electric_spark",
				"scrape",
				"infested" -> ParticleGroup.MAGIC;
			case "flame",
				"small_flame",
				"soul_fire_flame",
				"copper_fire_flame",
				"lava",
				"smoke",
				"large_smoke",
				"white_smoke",
				"campfire_cosy_smoke",
				"campfire_signal_smoke",
				"firework",
				"ash",
				"white_ash",
				"soul" -> ParticleGroup.FIRE;
			case "bubble",
				"bubble_pop",
				"bubble_column_up",
				"current_down",
				"splash",
				"rain",
				"underwater",
				"fishing",
				"dolphin",
				"squid_ink",
				"glow_squid_ink",
				"glow",
				"sulfur_bubbles",
				"cloud" -> ParticleGroup.WATER;
			case "dripping_lava",
				"falling_lava",
				"landing_lava",
				"dripping_water",
				"falling_water",
				"dripping_honey",
				"falling_honey",
				"landing_honey",
				"falling_nectar",
				"dripping_obsidian_tear",
				"falling_obsidian_tear",
				"landing_obsidian_tear",
				"dripping_dripstone_lava",
				"falling_dripstone_lava",
				"dripping_dripstone_water",
				"falling_dripstone_water" -> ParticleGroup.DRIPS;
			case "block",
				"block_marker",
				"block_crumble",
				"falling_dust",
				"dust",
				"dust_color_transition",
				"dust_plume",
				"dust_pillar",
				"item",
				"item_slime",
				"item_cobweb",
				"item_snowball",
				"egg_crack" -> ParticleGroup.BLOCKS;
			case "cherry_leaves",
				"pale_oak_leaves",
				"tinted_leaves",
				"mycelium",
				"spore_blossom_air",
				"falling_spore_blossom",
				"crimson_spore",
				"warped_spore",
				"snowflake",
				"firefly",
				"composter",
				"noxious_gas",
				"noxious_gas_cloud",
				"geyser",
				"geyser_base",
				"geyser_poof",
				"geyser_plume",
				"sulfur_cube_goo" -> ParticleGroup.NATURE;
			case "angry_villager",
				"happy_villager",
				"heart",
				"sneeze",
				"poof",
				"pause_mob_growth",
				"reset_mob_growth" -> ParticleGroup.MOBS;
			case "sculk_soul",
				"sculk_charge",
				"sculk_charge_pop",
				"shriek",
				"vibration" -> ParticleGroup.SCULK;
			case "portal",
				"reverse_portal",
				"end_rod",
				"nautilus" -> ParticleGroup.PORTALS;
			case "trial_spawner_detection",
				"trial_spawner_detection_ominous",
				"vault_connection",
				"ominous_spawning",
				"raid_omen",
				"trial_omen" -> ParticleGroup.TRIAL;
			default -> fromPath(path);
		};
	}

	private static ParticleGroup fromPath(String path) {
		if (containsAny(path, "drip", "falling", "landing")) {
			return ParticleGroup.DRIPS;
		}

		if (containsAny(path, "flame", "smoke", "lava", "fire", "ash")) {
			return ParticleGroup.FIRE;
		}

		if (containsAny(path, "bubble", "splash", "rain", "underwater", "water", "fishing")) {
			return ParticleGroup.WATER;
		}

		if (containsAny(path, "sculk", "shriek")) {
			return ParticleGroup.SCULK;
		}

		if (path.contains("portal")) {
			return ParticleGroup.PORTALS;
		}

		if (containsAny(path, "explosion", "crit", "gust", "sweep", "sonic")) {
			return ParticleGroup.COMBAT;
		}

		if (containsAny(path, "block", "item", "dust")) {
			return ParticleGroup.BLOCKS;
		}

		if (containsAny(path, "effect", "enchant", "witch", "omen")) {
			return ParticleGroup.MAGIC;
		}

		return ParticleGroup.OTHER;
	}

	private static boolean containsAny(String path, String... parts) {
		for (String part : parts) {
			if (path.contains(part)) {
				return true;
			}
		}

		return false;
	}
}
