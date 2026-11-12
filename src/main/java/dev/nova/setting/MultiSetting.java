package dev.nova.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** A group of named on/off chips, e.g. the target types of an aura. */
public class MultiSetting extends Setting<Map<String, Boolean>> {
	public MultiSetting(String name) {
		super(name, new LinkedHashMap<>());
	}

	/** Declares an item with its default state. */
	public MultiSetting add(String item, boolean enabled) {
		value.put(item, enabled);
		return this;
	}

	public boolean is(String item) {
		return value.getOrDefault(item, false);
	}

	public void toggle(String item) {
		value.computeIfPresent(item, (key, enabled) -> !enabled);
	}

	public Set<String> items() {
		return value.keySet();
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		value.forEach(json::addProperty);
		return json;
	}

	@Override
	public void fromJson(JsonElement json) {
		JsonObject object = json.getAsJsonObject();
		for (String item : value.keySet()) {
			if (object.has(item)) value.put(item, object.get(item).getAsBoolean());
		}
	}
}
