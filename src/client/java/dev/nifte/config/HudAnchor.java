package dev.nifte.config;

public enum HudAnchor {
	TOP_LEFT("Top left"),
	TOP_RIGHT("Top right"),
	BOTTOM_LEFT("Bottom left"),
	BOTTOM_RIGHT("Bottom right");

	private final String label;

	HudAnchor(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return this.label;
	}
}
