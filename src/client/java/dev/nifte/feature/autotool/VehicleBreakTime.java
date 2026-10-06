package dev.nifte.feature.autotool;

final class VehicleBreakTime {
	static final int NEVER = Integer.MAX_VALUE;

	private static final float BREAKS_ABOVE = 40.0F;
	private static final float DAMAGE_PER_POINT = 10.0F;
	private static final int MAX_HITS = 100;
	private static final double MAX_GAP_TICKS = 10_000.0;

	private VehicleBreakTime() {
	}

	static int ticksUntilBreak(float attackDamage, float attackSpeed) {
		if (attackDamage <= 0.0F || attackSpeed <= 0.0F) {
			return NEVER;
		}

		double gapValue = Math.ceil(20.0 / attackSpeed);
		if (gapValue > MAX_GAP_TICKS) {
			return NEVER;
		}

		int gap = Math.max(1, (int) gapValue);
		float accumulated = 0.0F;
		for (int hit = 0; hit < MAX_HITS; hit++) {
			if (hit > 0) {
				accumulated = Math.max(0.0F, accumulated - gap);
			}

			accumulated += attackDamage * DAMAGE_PER_POINT;
			if (accumulated > BREAKS_ABOVE) {
				if (hit == 0) {
					return 0;
				}

				long ticks = (long) hit * gap;
				return ticks >= NEVER ? NEVER : (int) ticks;
			}
		}

		return NEVER;
	}
}
