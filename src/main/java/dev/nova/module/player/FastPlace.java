package dev.nova.module.player;

import dev.nova.mixin.MinecraftClientAccessor;
import dev.nova.module.Category;
import dev.nova.module.Module;

public class FastPlace extends Module {
	public FastPlace() {
		super("FastPlace", "Убирает задержку между установкой блоков", Category.PLAYER);
	}

	@Override
	public void onTick() {
		((MinecraftClientAccessor) mc).nova$setItemUseCooldown(0);
	}
}
