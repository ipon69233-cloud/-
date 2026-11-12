package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;

public class AutoWalk extends Module {
	public AutoWalk() {
		super("AutoWalk", "Сам идёт вперёд", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		mc.options.forwardKey.setPressed(true);
	}

	@Override
	protected void onDisable() {
		mc.options.forwardKey.setPressed(false);
	}
}
