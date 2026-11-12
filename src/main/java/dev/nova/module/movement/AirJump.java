package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;

public class AirJump extends Module {
	private boolean wasPressed;

	public AirJump() {
		super("AirJump", "Позволяет прыгать прямо в воздухе", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		boolean pressed = mc.options.jumpKey.isPressed();
		boolean airborne = !mc.player.isOnGround() && !mc.player.getAbilities().flying
			&& !mc.player.isTouchingWater() && !mc.player.isInLava() && !mc.player.isClimbing();
		if (pressed && !wasPressed && airborne && mc.currentScreen == null) mc.player.jump();
		wasPressed = pressed;
	}
}
