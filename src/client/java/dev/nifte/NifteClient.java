package dev.nifte;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import dev.nifte.config.NifteConfig;
import dev.nifte.feature.autototem.AutoTotemFeature;
import dev.nifte.feature.camera.AutoThirdPerson;
import dev.nifte.feature.elytra.AutoElytraFeature;
import dev.nifte.feature.quickuse.QuickUseFeature;
import dev.nifte.feature.recipes.RecipeFeatures;
import dev.nifte.hud.ArmorHud;
import dev.nifte.hud.FpsHud;
import dev.nifte.hud.PotionHud;
import dev.nifte.input.NifteKeybinds;

public final class NifteClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		NifteConfig.load();
		NifteKeybinds.register();
		AutoThirdPerson.register();
		QuickUseFeature.register();
		AutoTotemFeature.register();
		AutoElytraFeature.register();
		RecipeFeatures.register();
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Nifte.id("fps"), FpsHud::extract);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Nifte.id("armor"), ArmorHud::extract);
		HudElementRegistry.attachElementAfter(VanillaHudElements.MOB_EFFECTS, Nifte.id("potions"), PotionHud::extract);
		Nifte.LOGGER.info("Nifte initialized");
	}
}
