package dev.nifte.config;

public enum ArmorHudAnchor {
	TOP_LEFT("Top left"),
	TOP_RIGHT("Top right"),
	BOTTOM_LEFT("Bottom left"),
	BOTTOM_RIGHT("Bottom right"),
	HOTBAR("Hotbar");

	private final String label;

	ArmorHudAnchor(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return this.label;
	}
}
