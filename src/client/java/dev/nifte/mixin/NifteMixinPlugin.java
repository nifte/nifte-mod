package dev.nifte.mixin;

import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

public final class NifteMixinPlugin implements IMixinConfigPlugin {
	private static final String MODERN_HANDS = "FirstPersonHandsAndItemsRendererMixin";
	private static final String LEGACY_HANDS = "ItemInHandRendererMixin";
	private static final String MODERN_HANDS_VERSION = "26.3";

	private boolean modernHands;

	@Override
	public void onLoad(String mixinPackage) {
		modernHands = minecraftAtLeast(MODERN_HANDS_VERSION);
	}

	@Override
	public List<String> getMixins() {
		return List.of(modernHands ? MODERN_HANDS : LEGACY_HANDS);
	}

	private static boolean minecraftAtLeast(String version) {
		try {
			Version minecraft = FabricLoader.getInstance()
				.getModContainer("minecraft")
				.orElseThrow()
				.getMetadata()
				.getVersion();
			return minecraft.compareTo(Version.parse(version)) >= 0;
		} catch (VersionParsingException exception) {
			throw new IllegalStateException("Could not read the Minecraft version", exception);
		}
	}
}
