package dev.nifte.input;

import com.mojang.blaze3d.platform.InputConstants;

public final class KeyboardKeys {
	public static final InputConstants.Type KEYBOARD = InputConstants.Type.KEYBOARD;

	private KeyboardKeys() {
	}

	public static boolean isDown(InputConstants.Key key) {
		return key.getType() == KEYBOARD && InputConstants.isKeyDown(key.getValue());
	}
}
