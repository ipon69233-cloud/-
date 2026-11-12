package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/** The movement itself is cancelled in EntityMixin. */
public class AirStuck extends Module {
	public AirStuck() {
		super("AirStuck", "Зависание в воздухе на месте", Category.MOVEMENT);
	}

	public boolean freezes(Entity entity) {
		return entity == mc.player;
	}

	@Override
	public void onTick() {
		mc.player.setVelocity(Vec3d.ZERO);
	}
}
