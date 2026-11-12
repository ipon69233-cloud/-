package dev.nova.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.List;

public class ModeSetting extends Setting<String> {
	private final List<String> modes;

	public ModeSetting(String name, String value, String... modes) {
		super(name, value);
		this.modes = List.of(modes);
	}

	public boolean is(String mode) {
		return value.equals(mode);
	}

	public List<String> getModes() {
		return modes;
	}

	@Override
	public void set(String mode) {
		if (modes.contains(mode)) value = mode;
	}

	public void cycle(boolean forward) {
		int index = modes.indexOf(value) + (forward ? 1 : -1);
		value = modes.get(Math.floorMod(index, modes.size()));
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(value);
	}

	@Override
	public void fromJson(JsonElement json) {
		String mode = json.getAsString();
		if (modes.contains(mode)) value = mode;
	}
}
