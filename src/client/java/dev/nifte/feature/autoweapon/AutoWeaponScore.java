package dev.nifte.feature.autoweapon;

final class AutoWeaponScore implements Comparable<AutoWeaponScore> {
	private final boolean canReach;
	private final float damage;
	private final boolean healthy;

	AutoWeaponScore(boolean canReach, float damage, boolean healthy) {
		this.canReach = canReach;
		this.damage = damage;
		this.healthy = healthy;
	}

	boolean isBetterThan(AutoWeaponScore other) {
		return compareTo(other) > 0;
	}

	@Override
	public int compareTo(AutoWeaponScore other) {
		int reach = Boolean.compare(this.canReach, other.canReach);
		if (reach != 0) {
			return reach;
		}

		int damageCompare = Float.compare(this.damage, other.damage);
		if (damageCompare != 0) {
			return damageCompare;
		}

		return Boolean.compare(this.healthy, other.healthy);
	}
}
