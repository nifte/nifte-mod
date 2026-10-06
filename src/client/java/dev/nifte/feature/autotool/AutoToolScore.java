package dev.nifte.feature.autotool;

final class AutoToolScore implements Comparable<AutoToolScore> {
	private final boolean canHarvest;
	private final int enchantmentPriority;
	private final float speed;

	AutoToolScore(boolean canHarvest, int enchantmentPriority, float speed) {
		this.canHarvest = canHarvest;
		this.enchantmentPriority = enchantmentPriority;
		this.speed = speed;
	}

	boolean isBetterThan(AutoToolScore other) {
		return compareTo(other) > 0;
	}

	@Override
	public int compareTo(AutoToolScore other) {
		int harvest = Boolean.compare(this.canHarvest, other.canHarvest);
		if (harvest != 0) {
			return harvest;
		}

		int enchantment = Integer.compare(this.enchantmentPriority, other.enchantmentPriority);
		if (enchantment != 0) {
			return enchantment;
		}

		return Float.compare(this.speed, other.speed);
	}
}
