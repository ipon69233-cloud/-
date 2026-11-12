package dev.nova.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.nova.NovaClient;
import dev.nova.gui.Theme;
import dev.nova.module.Module;
import dev.nova.module.Modules;
import dev.nova.setting.Setting;
import dev.nova.util.Friends;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persists the theme, friends, module states, binds and settings in config/nova.json. */
public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("nova.json");

	private Config() {
	}

	public static void load() {
		if (!Files.exists(FILE)) return;
		try {
			JsonObject root = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
			if (root.has("theme")) selectTheme(root.get("theme").getAsString());
			if (root.get("friends") instanceof JsonArray friends) {
				friends.forEach(name -> Friends.NAMES.add(name.getAsString()));
			}
			if (root.get("modules") instanceof JsonObject modules) {
				for (Module module : Modules.ALL) {
					if (modules.get(module.getName()) instanceof JsonObject json) read(module, json);
				}
			}
		} catch (IOException | RuntimeException e) {
			NovaClient.LOGGER.error("Could not read {}", FILE, e);
		}
	}

	private static void selectTheme(String name) {
		for (int i = 0; i < Theme.PALETTES.size(); i++) {
			if (Theme.PALETTES.get(i).name().equals(name)) Theme.select(i);
		}
	}

	private static void read(Module module, JsonObject json) {
		if (json.has("key")) module.setKey(json.get("key").getAsInt());
		if (json.get("settings") instanceof JsonObject settings) {
			for (Setting<?> setting : module.getSettings()) {
				JsonElement value = settings.get(setting.getName());
				if (value == null) continue;
				try {
					setting.fromJson(value);
				} catch (RuntimeException e) {
					// A value of an outdated shape keeps the default instead of dropping the whole file.
					NovaClient.LOGGER.warn("Ignoring saved value of {}.{}", module.getName(), setting.getName());
				}
			}
		}
		// Hold modules follow their key and momentary ones re-trigger, so neither is restored.
		if (json.has("enabled") && !module.isHold() && module != Modules.CLICK_GUI) {
			module.setEnabled(json.get("enabled").getAsBoolean());
		}
	}

	public static void save() {
		JsonObject root = new JsonObject();
		root.addProperty("theme", Theme.PALETTES.get(Theme.index()).name());
		JsonArray friends = new JsonArray();
		Friends.NAMES.forEach(friends::add);
		root.add("friends", friends);

		JsonObject modules = new JsonObject();
		for (Module module : Modules.ALL) {
			JsonObject json = new JsonObject();
			json.addProperty("enabled", module.isEnabled());
			json.addProperty("key", module.getKey());
			JsonObject settings = new JsonObject();
			for (Setting<?> setting : module.getSettings()) settings.add(setting.getName(), setting.toJson());
			json.add("settings", settings);
			modules.add(module.getName(), json);
		}
		root.add("modules", modules);
		try {
			Files.createDirectories(FILE.getParent());
			Files.writeString(FILE, GSON.toJson(root));
		} catch (IOException e) {
			NovaClient.LOGGER.error("Could not write {}", FILE, e);
		}
	}
}
