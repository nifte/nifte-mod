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
import dev.nifte.feature.restock.HandRestockFeature;
import dev.nifte.hud.ArmorHud;
import dev.nifte.hud.CompassHud;
import dev.nifte.hud.FpsHud;
import dev.nifte.hud.MobHealthHud;
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
		HandRestockFeature.register();
		RecipeFeatures.register();
		HudElementRegistry.attachElementBefore(VanillaHudElements.BOSS_BAR, Nifte.id("compass"), CompassHud::extract);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Nifte.id("fps"), FpsHud::extract);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Nifte.id("armor"), ArmorHud::extract);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Nifte.id("mob_health"), MobHealthHud::extract);
		HudElementRegistry.attachElementAfter(VanillaHudElements.MOB_EFFECTS, Nifte.id("potions"), PotionHud::extract);
		Nifte.LOGGER.info("Nifte initialized");
	}
}
