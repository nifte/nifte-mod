package dev.nifte.feature.autotool;

final class AutoToolScore implements Comparable<AutoToolScore> {
	private final boolean canHarvest;
	private final boolean appropriateTool;
	private final int enchantmentPriority;
	private final boolean preferredTool;
	private final float speed;
	private final boolean matchesTool;

	AutoToolScore(
		boolean canHarvest,
		boolean appropriateTool,
		int enchantmentPriority,
		boolean preferredTool,
		float speed,
		boolean matchesTool
	) {
		this.canHarvest = canHarvest;
		this.appropriateTool = appropriateTool;
		this.enchantmentPriority = enchantmentPriority;
		this.preferredTool = preferredTool;
		this.speed = speed;
		this.matchesTool = matchesTool;
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

		int appropriate = Boolean.compare(this.appropriateTool, other.appropriateTool);
		if (appropriate != 0) {
			return appropriate;
		}

		int enchantment = Integer.compare(this.enchantmentPriority, other.enchantmentPriority);
		if (enchantment != 0) {
			return enchantment;
		}

		int preferred = Boolean.compare(this.preferredTool, other.preferredTool);
		if (preferred != 0) {
			return preferred;
		}

		int speed = Float.compare(this.speed, other.speed);
		if (speed != 0) {
			return speed;
		}

		return Boolean.compare(this.matchesTool, other.matchesTool);
	}
}
