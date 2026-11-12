package dev.nova.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.nova.NovaClient;
import dev.nova.module.Module;
import dev.nova.module.Modules;
import dev.nova.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persists module states, binds and settings in config/nova.json. */
public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("nova.json");

	private Config() {
	}

	public static void load() {
		if (!Files.exists(FILE)) return;
		try {
			JsonObject root = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
			for (Module module : Modules.ALL) {
				if (root.get(module.getName()) instanceof JsonObject json) read(module, json);
			}
		} catch (IOException | RuntimeException e) {
			NovaClient.LOGGER.error("Could not read {}", FILE, e);
		}
	}

	private static void read(Module module, JsonObject json) {
		if (json.has("key")) module.setKey(json.get("key").getAsInt());
		if (json.get("settings") instanceof JsonObject settings) {
			for (Setting<?> setting : module.getSettings()) {
				JsonElement value = settings.get(setting.getName());
				if (value != null) setting.fromJson(value);
			}
		}
		// Hold modules follow their key and momentary ones re-trigger, so neither is restored.
		if (json.has("enabled") && !module.isHold() && module != Modules.CLICK_GUI) {
			module.setEnabled(json.get("enabled").getAsBoolean());
		}
	}

	public static void save() {
		JsonObject root = new JsonObject();
		for (Module module : Modules.ALL) {
			JsonObject json = new JsonObject();
			json.addProperty("enabled", module.isEnabled());
			json.addProperty("key", module.getKey());
			JsonObject settings = new JsonObject();
			for (Setting<?> setting : module.getSettings()) settings.add(setting.getName(), setting.toJson());
			json.add("settings", settings);
			root.add(module.getName(), json);
		}
		try {
			Files.createDirectories(FILE.getParent());
			Files.writeString(FILE, GSON.toJson(root));
		} catch (IOException e) {
			NovaClient.LOGGER.error("Could not write {}", FILE, e);
		}
	}
}
