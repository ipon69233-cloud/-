package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;

/** The slowdown is skipped in ClientPlayerEntityMixin. */
public class NoSlow extends Module {
	public NoSlow() {
		super("NoSlow", "Не замедляет при еде, луке и щите", Category.MOVEMENT);
	}
}
