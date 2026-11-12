package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;

public class Sprint extends Module {
	public Sprint() {
		super("Sprint", "Всегда бежит при движении вперёд", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (!mc.options.forwardKey.isPressed() || mc.player.isSneaking() || mc.player.horizontalCollision) return;
		if (!mc.player.getHungerManager().canSprint() && !mc.player.getAbilities().allowFlying) return;
		mc.player.setSprinting(true);
	}
}
