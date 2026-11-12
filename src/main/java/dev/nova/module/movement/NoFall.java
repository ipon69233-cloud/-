package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;

/** The on-ground flag is rewritten in ClientPlayerEntityMixin. */
public class NoFall extends Module {
	public NoFall() {
		super("NoFall", "Убирает урон от падения", Category.MOVEMENT);
	}

	/** Whether outgoing movement packets should claim the player is on the ground. */
	public boolean shouldSpoofGround() {
		// Claiming ground contact while gliding would stop the elytra.
		return mc.player != null && !mc.player.isGliding() && mc.player.fallDistance > 2.0;
	}
}
