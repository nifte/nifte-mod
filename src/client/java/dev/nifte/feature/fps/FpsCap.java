package dev.nifte.feature.fps;

import com.mojang.serialization.Codec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

public final class FpsCap {
	public static final int MAX_FPS = 500;
	public static final int UNLIMITED = MAX_FPS + 10;

	private static final int MIN_FPS = 10;
	private static final int DEFAULT_FPS = 120;
	private static final int VANILLA_UNLIMITED = 260;
	// One step is skipped so the old Unlimited value, 260, is not also a real cap.
	private static final int SLIDER_MAX_INDEX = UNLIMITED / MIN_FPS - 1;

	private FpsCap() {
	}

	public static OptionInstance<Integer> framerateLimit() {
		return new OptionInstance<>(
			"options.framerateLimit",
			OptionInstance.noTooltip(),
			FpsCap::label,
			new OptionInstance.IntRange(1, SLIDER_MAX_INDEX).xmap(FpsCap::toFps, FpsCap::toIndex, true),
			Codec.intRange(MIN_FPS, UNLIMITED),
			DEFAULT_FPS,
			fps -> Minecraft.getInstance().getFramerateLimitTracker().setFramerateLimit(fps)
		);
	}

	private static Component label(Component caption, Integer fps) {
		if (fps >= UNLIMITED) {
			return Options.genericValueLabel(caption, Component.translatable("options.framerateLimit.max"));
		}

		return Options.genericValueLabel(caption, Component.translatable("options.framerate", fps));
	}

	private static int toFps(int index) {
		if (index >= SLIDER_MAX_INDEX) {
			return UNLIMITED;
		}

		int fps = index * MIN_FPS;
		if (fps >= VANILLA_UNLIMITED) {
			fps += MIN_FPS;
		}

		return fps;
	}

	private static int toIndex(int fps) {
		if (fps >= UNLIMITED || fps == VANILLA_UNLIMITED) {
			return SLIDER_MAX_INDEX;
		}

		if (fps > VANILLA_UNLIMITED) {
			return fps / MIN_FPS - 1;
		}

		return fps / MIN_FPS;
	}
}
