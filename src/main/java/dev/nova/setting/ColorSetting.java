package dev.nova.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** An opaque color stored as 0xAARRGGBB, picked from a fixed palette in the menu. */
public class ColorSetting extends Setting<Integer> {
	public static final int[] PALETTE = {
		0xFFFF4D4D, 0xFFFF7A1A, 0xFFFFB020, 0xFFFFE04D, 0xFF8CE04D, 0xFF2ED573, 0xFF1ABC9C,
		0xFF20D0E0, 0xFF3B9BFF, 0xFF5B6BFF, 0xFF8B5BFF, 0xFFC05BFF, 0xFFFF5BC0, 0xFFFFFFFF
	};

	public ColorSetting(String name, int color) {
		super(name, color | 0xFF000000);
	}

	@Override
	public void set(Integer color) {
		value = color | 0xFF000000;
	}

	/** The color with the given alpha, 0..255. */
	public int withAlpha(int alpha) {
		return value & 0xFFFFFF | alpha << 24;
	}

	public String hex() {
		return String.format("#%06X", value & 0xFFFFFF);
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(hex());
	}

	@Override
	public void fromJson(JsonElement json) {
		set(Integer.parseInt(json.getAsString().substring(1), 16));
	}
}
