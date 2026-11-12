package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;

/** The packet handling lives in ClientPlayNetworkHandlerMixin. */
public class Velocity extends Module {
	public Velocity() {
		super("Velocity", "Убирает отбрасывание от ударов и взрывов", Category.COMBAT);
	}
}
