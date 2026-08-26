package dev.nifte.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;

public final class PotionEffectHudLayout {
	private static final int ICON_SPACING = 25;
	private static final int HARMFUL_ROW_OFFSET = 26;
	private static final int DEMO_OFFSET = 15;

	private PotionEffectHudLayout() {
	}

	public static List<PotionEffectIcon> vanillaIcons(Minecraft minecraft, int guiWidth) {
		List<PotionEffectIcon> icons = new ArrayList<>();
		if (minecraft.player == null) {
			return icons;
		}

		if (minecraft.gui.screen() != null && minecraft.gui.screen().showsActiveEffects()) {
			return icons;
		}

		int beneficialCount = 0;
		int harmfulCount = 0;
		List<MobEffectInstance> effects = new ArrayList<>(minecraft.player.getActiveEffects());
		effects.sort(Comparator.reverseOrder());
		for (MobEffectInstance instance : effects) {
			if (!instance.showIcon()) {
				continue;
			}

			int x = guiWidth;
			int y = 1;
			if (minecraft.isDemo()) {
				y += DEMO_OFFSET;
			}

			if (instance.getEffect().value().isBeneficial()) {
				beneficialCount++;
				x -= ICON_SPACING * beneficialCount;
			} else {
				harmfulCount++;
				x -= ICON_SPACING * harmfulCount;
				y += HARMFUL_ROW_OFFSET;
			}

			icons.add(new PotionEffectIcon(instance, x, y));
		}

		return icons;
	}
}
