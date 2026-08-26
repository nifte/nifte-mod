package dev.nifte.feature.ping;

public final class NumericalPing {
	private NumericalPing() {
	}

	public static int color(int latency) {
		if (latency < 0) {
			return 0xAAAAAA;
		}

		if (latency < 150) {
			return 0x55FF55;
		}

		if (latency < 300) {
			return 0xFFFF55;
		}

		return 0xFF5555;
	}
}
