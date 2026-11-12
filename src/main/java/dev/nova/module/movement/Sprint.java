package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import dev.nova.util.MoveUtil;

/** Sprinting without forward input is allowed by InputMixin. */
public class Sprint extends Module {
	private final BoolSetting omnidirectional = bool("Во все стороны", true);

	public Sprint() {
		super("Sprint", "Постоянный спринт во все стороны", Category.MOVEMENT);
	}

	public boolean isOmnidirectional() {
		return omnidirectional.get();
	}

	@Override
	public void onTick() {
		boolean moving = omnidirectional.get() ? MoveUtil.isMoving() : mc.options.forwardKey.isPressed();
		if (!moving || mc.player.isSneaking() || mc.player.horizontalCollision) return;
		if (!mc.player.getHungerManager().canSprint() && !mc.player.getAbilities().allowFlying) return;
		mc.player.setSprinting(true);
	}
}
