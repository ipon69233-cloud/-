package dev.nova.gui;

import dev.nova.gui.render.Shapes;

import java.util.List;

/** Color themes of the menu and HUD. The derived colors are recomputed when a palette is selected. */
public final class Theme {
	public record Palette(String name, int accent, int background, int surface, int tint, int muted) {
	}

	public static final List<Palette> PALETTES = List.of(
		new Palette("Nova", 0x5C7CFF, 0x0A0B10, 0x11131A, 0x1E2233, 0x8E94A6),
		new Palette("Cherry", 0xFF4D6D, 0x120A0B, 0x1A1012, 0x2E1B1F, 0xA68E92),
		new Palette("Sun", 0xFFB703, 0x12100A, 0x1A1710, 0x2E281B, 0xA6A08E),
		new Palette("Mint", 0x00F5D4, 0x091210, 0x0E1A17, 0x172E28, 0x8DA69F),
		new Palette("Aqua", 0x00BBF9, 0x091012, 0x0E181A, 0x17282E, 0x8E9EA6),
		new Palette("Amethyst", 0x9D4EDD, 0x0E0912, 0x150E1A, 0x25172E, 0x998EA6),
		new Palette("Rose", 0xF15BB5, 0x120910, 0x1A0E17, 0x2E1728, 0xA68E9D),
		new Palette("Nebula", 0x7D7CFF, 0x080B16, 0x101327, 0x22295A, 0x9BA2C6),
		new Palette("Aurora", 0x60F5C8, 0x07120F, 0x0D1A18, 0x163B34, 0x91B5AD),
		new Palette("Volt", 0xCEFF45, 0x0F1207, 0x171B0B, 0x343D12, 0xADB98E),
		new Palette("Cyber", 0x38F2FF, 0x061014, 0x0A171E, 0x103C47, 0x91B5BC),
		new Palette("Matrix", 0x5CFF7A, 0x071207, 0x0D1B0E, 0x173C1C, 0x92B897),
		new Palette("Bloodmoon", 0xFF405D, 0x140607, 0x1D0A0D, 0x451319, 0xBC8E95),
		new Palette("Synthwave", 0xFF38B5, 0x0B0716, 0x140C24, 0x301851, 0xB298C5),
		new Palette("Galaxy", 0x9B7FFF, 0x05060F, 0x0E0F1F, 0x1F2148, 0x8E8FBF),
		new Palette("Lava", 0xFF6B3D, 0x140706, 0x1F0D0A, 0x4D1F12, 0xC09084),
		new Palette("Frost", 0x6BD3FF, 0x070F14, 0x0E1A20, 0x183842, 0x95B6BD),
		new Palette("Sakura", 0xFFA0CC, 0x140A0F, 0x1E1018, 0x402235, 0xC295AA)
	);

	public static final int TEXT = 0xFFFFFFFF;

	/** Bright and deep ends of the accent gradient. */
	public static int accent, accentDeep;
	public static int accentSoft;
	public static int background, surface;
	public static int textSoft, muted;
	/** Translucent overlays of rising strength, used for fills and borders. */
	public static int w01, w02, w03, w04, w06, w08, w12, w16, w24;

	private static int index = -1;

	static {
		select(0);
	}

	private Theme() {
	}

	public static int index() {
		return index;
	}

	public static void select(int newIndex) {
		index = Math.floorMod(newIndex, PALETTES.size());
		Palette palette = PALETTES.get(index);
		accent = lighten(palette.accent, 0.28f);
		accentDeep = palette.accent | 0xFF000000;
		accentSoft = alpha(accent, 0.16f);
		// No backdrop blur behind the panels, so they are a little more opaque than glass.
		background = alpha(palette.background, 0.86f);
		surface = alpha(palette.surface, 0.93f);
		textSoft = alpha(palette.muted, 0.95f);
		muted = alpha(palette.muted, 0.62f);
		w01 = alpha(palette.tint, 0.25f);
		w02 = alpha(palette.tint, 0.38f);
		w03 = alpha(palette.tint, 0.52f);
		w04 = alpha(palette.tint, 0.62f);
		w06 = alpha(palette.tint, 0.80f);
		w08 = alpha(palette.tint, 0.95f);
		w12 = Shapes.mix(palette.tint | 0xFF000000, palette.muted | 0xFF000000, 0.18f);
		w16 = Shapes.mix(palette.tint | 0xFF000000, palette.muted | 0xFF000000, 0.32f);
		w24 = alpha(palette.muted, 0.42f);
	}

	public static int alpha(int color, float alpha) {
		return color & 0xFFFFFF | Math.round(Math.clamp(alpha, 0f, 1f) * 255) << 24;
	}

	/** Scales the existing alpha of a color. */
	public static int fade(int color, float factor) {
		return color & 0xFFFFFF | Math.round((color >>> 24) * Math.clamp(factor, 0f, 1f)) << 24;
	}

	public static int mix(int from, int to, float t) {
		return Shapes.mix(from, to, Math.clamp(t, 0f, 1f));
	}

	private static int lighten(int rgb, float amount) {
		return Shapes.mix(rgb | 0xFF000000, 0xFFFFFFFF, amount);
	}
}
