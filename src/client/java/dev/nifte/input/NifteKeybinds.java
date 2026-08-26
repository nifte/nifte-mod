package dev.nifte.input;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

import dev.nifte.Nifte;
import dev.nifte.config.NifteConfig;
import dev.nifte.config.NifteConfigScreen;
import dev.nifte.feature.autotool.AutoToolFeature;
import dev.nifte.feature.autoweapon.AutoWeaponFeature;
import dev.nifte.feature.fullbright.FullbrightFeature;
import dev.nifte.feature.zoom.ZoomFeature;

public final class NifteKeybinds {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Nifte.id("nifte"));

	public static KeyMapping openConfig;
	public static KeyMapping fullbright;
	public static KeyMapping autoTool;
	public static KeyMapping autoWeapon;
	public static KeyMapping zoom;

	private NifteKeybinds() {
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
		zoom = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.nifte.zoom",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_Z,
			CATEGORY
		));

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

		ZoomFeature.tick(NifteConfig.get().zoomToggleMode && zoom.consumeClick());
	}
}
