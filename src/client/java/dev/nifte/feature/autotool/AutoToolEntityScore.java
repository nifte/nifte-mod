package dev.nifte.feature.autotool;

final class AutoToolEntityScore implements Comparable<AutoToolEntityScore> {
	private final boolean canReach;
	private final int ticksToBreak;
	private final float damage;

	AutoToolEntityScore(boolean canReach, int ticksToBreak, float damage) {
		this.canReach = canReach;
		this.ticksToBreak = ticksToBreak;
		this.damage = damage;
	}

	boolean isBetterThan(AutoToolEntityScore other) {
		return compareTo(other) > 0;
	}

	@Override
	public int compareTo(AutoToolEntityScore other) {
		int reach = Boolean.compare(this.canReach, other.canReach);
		if (reach != 0) {
			return reach;
		}

		if (this.ticksToBreak == VehicleBreakTime.NEVER && other.ticksToBreak == VehicleBreakTime.NEVER) {
			return 0;
		}

		int ticks = Integer.compare(other.ticksToBreak, this.ticksToBreak);
		if (ticks != 0) {
			return ticks;
		}

		return Float.compare(this.damage, other.damage);
	}
}
