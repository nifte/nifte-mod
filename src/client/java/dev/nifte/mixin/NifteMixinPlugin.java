package dev.nifte.mixin;

import net.fabricmc.loader.api.FabricLoader;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

public final class NifteMixinPlugin implements IMixinConfigPlugin {
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (targetClassName.startsWith("net.caffeinemc.mods.sodium.")) {
			return FabricLoader.getInstance().isModLoaded("sodium");
		}

		return true;
	}
}
