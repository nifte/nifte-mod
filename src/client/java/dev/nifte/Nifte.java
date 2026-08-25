package dev.nifte;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Nifte {
	public static final String MOD_ID = "nifte";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private Nifte() {
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
