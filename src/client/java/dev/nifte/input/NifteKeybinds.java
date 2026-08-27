package dev.nifte.input;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

import dev.nifte.Nifte;
import dev.nifte.config.NifteConfigScreen;
import dev.nifte.feature.autotool.AutoToolFeature;
import dev.nifte.feature.autoweapon.AutoWeaponFeature;
import dev.nifte.feature.food.QuickEatFeature;
import dev.nifte.feature.fullbright.FullbrightFeature;
import dev.nifte.feature.glow.EntityGlowFeature;
import dev.nifte.feature.zoom.ZoomFeature;

public final class NifteKeybinds {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Nifte.id("nifte"));

	public static KeyMapping openConfig;
	public static KeyMapping fullbright;
	public static KeyMapping autoTool;
	public static KeyMapping autoWeapon;
	public static KeyMapping highlightHostileMobs;
	public static KeyMapping highlightOtherPlayers;
	public static KeyMapping zoom;
	public static KeyMapping quickEat;
	public static final KeyMapping[] quickUseSlots = new KeyMapping[9];

	private NifteKeybinds() {
	}

	public static boolean isBound(KeyMapping mapping) {
		return mapping != null && !mapping.isUnbound();
	}

	public static void register() {
		openConfig = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.nifte.open_config",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_N,
			CATEGORY
		));
		fullbright = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.nifte.fullbright",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			CATEGORY
		));
		autoTool = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.nifte.auto_tool",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			CATEGORY
		));
		autoWeapon = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.nifte.auto_weapon",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			CATEGORY
		));
		highlightHostileMobs = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.nifte.highlight_hostile_mobs",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			CATEGORY
		));
		highlightOtherPlayers = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.nifte.highlight_other_players",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			CATEGORY
		));
		zoom = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.nifte.zoom",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_Z,
			CATEGORY
		));
		quickEat = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.nifte.quick_eat",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			CATEGORY
		));
		for (int slot = 0; slot < quickUseSlots.length; slot++) {
			quickUseSlots[slot] = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.nifte.quick_use." + (slot + 1),
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN,
				CATEGORY
			));
		}

		ClientTickEvents.END_CLIENT_TICK.register(NifteKeybinds::tick);
	}

	private static void tick(Minecraft minecraft) {
		while (openConfig.consumeClick()) {
			minecraft.gui.setScreen(NifteConfigScreen.create(minecraft.gui.screen()));
		}

		while (fullbright.consumeClick()) {
			FullbrightFeature.toggle();
		}

		while (autoTool.consumeClick()) {
			AutoToolFeature.toggle();
		}

		while (autoWeapon.consumeClick()) {
			AutoWeaponFeature.toggle();
		}

		while (highlightHostileMobs.consumeClick()) {
			EntityGlowFeature.toggleHostileMobs();
		}

		while (highlightOtherPlayers.consumeClick()) {
			EntityGlowFeature.toggleOtherPlayers();
		}

		ZoomFeature.tick();
		QuickEatFeature.tick(minecraft);
	}
}
