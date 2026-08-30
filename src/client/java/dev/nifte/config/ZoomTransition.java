package dev.nifte.config;

public enum ZoomTransition {
	SMOOTH("Smooth"),
	INSTANT("Instant");

	private final String label;

	ZoomTransition(String label) {
		this.label = label;
	}

	public boolean isSmooth() {
		return this == SMOOTH;
	}

	@Override
	public String toString() {
		return this.label;
	}
}
