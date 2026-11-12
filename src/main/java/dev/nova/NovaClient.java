package dev.nova;

import dev.nova.config.Config;
import dev.nova.module.Module;
import dev.nova.module.Modules;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NovaClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Nova");

	@Override
	public void onInitializeClient() {
		Config.load();
		LOGGER.info("Nova Client loaded with {} modules", Modules.ALL.size());
	}

	private static boolean inGame() {
		MinecraftClient mc = MinecraftClient.getInstance();
		return mc.player != null && mc.world != null;
	}

	/** Called at the start of every client tick. */
	public static void onTick() {
		if (!inGame()) return;
		for (Module module : Modules.ALL) {
			if (module.isEnabled()) module.onTick();
		}
	}

	/** Called for key presses and releases while no screen is open. */
	public static void onKey(int key, boolean pressed) {
		if (!inGame()) return;
		boolean changed = false;
		for (Module module : Modules.ALL) {
			if (module.getKey() != key) continue;
			module.onKey(pressed);
			changed |= !module.isHold();
		}
		if (changed && pressed) Config.save();
	}

	/** Called every frame after the vanilla HUD. */
	public static void onRender2D(DrawContext context, float tickDelta) {
		if (!inGame()) return;
		for (Module module : Modules.ALL) {
			if (module.isEnabled()) module.onRender2D(context, tickDelta);
		}
	}
}
