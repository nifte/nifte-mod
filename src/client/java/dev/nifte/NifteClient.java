package dev.nifte;

import net.fabricmc.api.ClientModInitializer;

public final class NifteClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Nifte.LOGGER.info("Nifte initialized");
	}
}
