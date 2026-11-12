package dev.nova.module.player;

import dev.nova.mixin.ClientPlayerInteractionManagerAccessor;
import dev.nova.module.Category;
import dev.nova.module.Module;

public class FastBreak extends Module {
	public FastBreak() {
		super("FastBreak", "Убирает задержку между ломанием блоков", Category.PLAYER);
	}

	@Override
	public void onTick() {
		((ClientPlayerInteractionManagerAccessor) mc.interactionManager).nova$setBlockBreakingCooldown(0);
	}
}
