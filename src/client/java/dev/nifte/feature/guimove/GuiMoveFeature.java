package dev.nifte.feature.guimove;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.BookSignScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

import dev.nifte.config.NifteConfig;

public final class GuiMoveFeature {
	private GuiMoveFeature() {
	}

	public static boolean shouldApply() {
		if (!NifteConfig.get().guiMoveEnabled) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.gui.screen();
		if (screen == null) {
			return false;
		}

		if (screen.isPauseScreen()) {
			return false;
		}

		if (screen instanceof ChatScreen
			|| screen instanceof AbstractSignEditScreen
			|| screen instanceof BookEditScreen
			|| screen instanceof BookViewScreen
			|| screen instanceof BookSignScreen
			|| screen instanceof AbstractCommandBlockEditScreen
			|| screen instanceof AnvilScreen && screen.getFocused() instanceof EditBox) {
			return false;
		}

		return !(screen.getFocused() instanceof EditBox);
	}

	public static Input readKeyPresses(Options options) {
		Window window = Minecraft.getInstance().getWindow();
		return new Input(
			isDown(window, options.keyUp),
			isDown(window, options.keyDown),
			isDown(window, options.keyLeft),
			isDown(window, options.keyRight),
			isDown(window, options.keyJump),
			isDown(window, options.keyShift),
			isDown(window, options.keySprint)
		);
	}

	public static Vec2 movementVector(Input presses) {
		float forward = impulse(presses.forward(), presses.backward());
		float left = impulse(presses.left(), presses.right());
		return new Vec2(left, forward).normalized();
	}

	private static boolean isDown(Window window, KeyMapping mapping) {
		InputConstants.Key key = KeyMappingHelper.getBoundKeyOf(mapping);
		return key.getType() == InputConstants.Type.KEYSYM && InputConstants.isKeyDown(window, key.getValue());
	}

	private static float impulse(boolean positive, boolean negative) {
		if (positive == negative) {
			return 0.0F;
		}

		return positive ? 1.0F : -1.0F;
	}
}
