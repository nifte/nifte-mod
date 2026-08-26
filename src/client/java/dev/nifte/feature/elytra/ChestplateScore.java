package dev.nifte.feature.elytra;

final class ChestplateScore implements Comparable<ChestplateScore> {
	private final double armor;
	private final double toughness;
	private final int protection;
	private final boolean healthy;
	private final int remainingDurability;

	ChestplateScore(double armor, double toughness, int protection, boolean healthy, int remainingDurability) {
		this.armor = armor;
		this.toughness = toughness;
		this.protection = protection;
		this.healthy = healthy;
		this.remainingDurability = remainingDurability;
	}

	boolean isBetterThan(ChestplateScore other) {
		return compareTo(other) > 0;
	}

	@Override
	public int compareTo(ChestplateScore other) {
		int armorCompare = Double.compare(this.armor, other.armor);
		if (armorCompare != 0) {
			return armorCompare;
		}

		int toughnessCompare = Double.compare(this.toughness, other.toughness);
		if (toughnessCompare != 0) {
			return toughnessCompare;
		}

		int protectionCompare = Integer.compare(this.protection, other.protection);
		if (protectionCompare != 0) {
			return protectionCompare;
		}

		int healthyCompare = Boolean.compare(this.healthy, other.healthy);
		if (healthyCompare != 0) {
			return healthyCompare;
		}

		return Integer.compare(this.remainingDurability, other.remainingDurability);
	}
}
