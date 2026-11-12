package dev.nova.gui;

public final class Theme {
	public static final int ACCENT = 0xFF8B5CF6;
	public static final int ACCENT_ALT = 0xFF22D3EE;
	public static final int PANEL = 0xE0121218;
	public static final int ROW_HOVER = 0x30FFFFFF;
	public static final int SETTINGS_BG = 0xE01B1B24;
	public static final int TEXT = 0xFFFFFFFF;
	public static final int TEXT_DIM = 0xFFA0A0AC;

	private Theme() {
	}

	/** Blends the two accent colors, cycling over time with a per-row phase offset. */
	public static int gradient(int index) {
		double phase = (System.currentTimeMillis() % 3000L) / 3000.0 * Math.PI * 2 + index * 0.35;
		return mix(ACCENT, ACCENT_ALT, (float) (Math.sin(phase) * 0.5 + 0.5));
	}

	public static int mix(int from, int to, float t) {
		int a = channel(from >>> 24, to >>> 24, t);
		int r = channel(from >> 16 & 0xFF, to >> 16 & 0xFF, t);
		int g = channel(from >> 8 & 0xFF, to >> 8 & 0xFF, t);
		int b = channel(from & 0xFF, to & 0xFF, t);
		return a << 24 | r << 16 | g << 8 | b;
	}

	private static int channel(int from, int to, float t) {
		return Math.round(from + (to - from) * t);
	}
}
