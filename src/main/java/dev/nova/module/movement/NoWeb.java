package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import net.minecraft.entity.Entity;

/** The slowdown is cancelled in EntityMixin. */
public class NoWeb extends Module {
	public NoWeb() {
		super("NoWeb", "Убирает замедление в паутине", Category.MOVEMENT);
	}

	public boolean ignores(Entity entity) {
		return entity == mc.player;
	}
}
