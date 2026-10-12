package dev.nifte.feature.fps;

import com.mojang.serialization.Codec;

import net.minecraft.client.FramerateLimiter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

public final class FpsCap {
	public static final int MAX_FPS = 500;
	public static final int UNLIMITED = MAX_FPS + 10;
	public static final int VANILLA_UNLIMITED = 260;

	private static final int MIN_FPS = 10;
	private static final int DEFAULT_FPS = 120;

	private FpsCap() {
	}

	public static boolean isUnlimited(int fps) {
		return fps >= UNLIMITED;
	}

	public static void limitAboveVanilla(Minecraft minecraft) {
		int fps = minecraft.gameRenderer.gameRenderState().framerateLimit;
		if (fps >= VANILLA_UNLIMITED && fps < UNLIMITED) {
			FramerateLimiter.limitDisplayFPS(fps);
		}
	}

	public static OptionInstance<Integer> framerateLimit() {
		return new OptionInstance<>(
			"options.framerateLimit",
			OptionInstance.noTooltip(),
			FpsCap::label,
			new OptionInstance.IntRange(1, UNLIMITED / MIN_FPS).xmap(index -> index * MIN_FPS, fps -> fps / MIN_FPS, true),
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
}
