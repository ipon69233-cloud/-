package dev.nova.module.player;

import dev.nova.module.Category;
import dev.nova.module.Module;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoRespawn extends Module {
	public AutoRespawn() {
		super("AutoRespawn", "Мгновенно возрождает после смерти", Category.PLAYER);
	}

	@Override
	public void onTick() {
		if (!(mc.currentScreen instanceof DeathScreen)) return;
		mc.player.requestRespawn();
		mc.setScreen(null);
	}
}
