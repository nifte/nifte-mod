package dev.nifte.feature.autoweapon;

final class AutoWeaponScore implements Comparable<AutoWeaponScore> {
	private final boolean canReach;
	private final float damage;

	AutoWeaponScore(boolean canReach, float damage) {
		this.canReach = canReach;
		this.damage = damage;
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

		return Float.compare(this.damage, other.damage);
	}
}
