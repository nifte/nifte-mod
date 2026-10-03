package dev.nifte.input;

import java.lang.reflect.Method;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;

public final class KeyboardKeys {
	public static final InputConstants.Type KEYBOARD = keyboardType();

	private static final Method IS_KEY_DOWN = isKeyDownMethod();
	private static final boolean IS_KEY_DOWN_NEEDS_WINDOW = IS_KEY_DOWN.getParameterCount() == 2;

	private KeyboardKeys() {
	}

	public static boolean isDown(InputConstants.Key key) {
		if (key.getType() != KEYBOARD) {
			return false;
		}

		try {
			if (IS_KEY_DOWN_NEEDS_WINDOW) {
				Window window = Minecraft.getInstance().getWindow();
				return (boolean) IS_KEY_DOWN.invoke(null, window, key.getValue());
			}
			return (boolean) IS_KEY_DOWN.invoke(null, key.getValue());
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Failed to read keyboard state", exception);
		}
	}

	private static InputConstants.Type keyboardType() {
		try {
			return Enum.valueOf(InputConstants.Type.class, "KEYBOARD");
		} catch (IllegalArgumentException missing) {
			return Enum.valueOf(InputConstants.Type.class, "KEYSYM");
		}
	}

	private static Method isKeyDownMethod() {
		try {
			return InputConstants.class.getMethod("isKeyDown", int.class);
		} catch (NoSuchMethodException missing) {
			try {
				return InputConstants.class.getMethod("isKeyDown", Window.class, int.class);
			} catch (NoSuchMethodException stillMissing) {
				throw new IllegalStateException("InputConstants.isKeyDown is missing", stillMissing);
			}
		}
	}
}
