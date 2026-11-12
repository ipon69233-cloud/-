package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

/** Replaces the elytra physics for the local player, see LivingEntityMixin. */
public class DragonFly extends Module {
	private final NumberSetting speed = number("Скорость", 1.6, 0.5, 4.0, 0.1);

	public DragonFly() {
		super("DragonFly", "Полёт на элитрах без ограничений", Category.MOVEMENT);
	}

	/** Moves the gliding player itself. Returns false when vanilla physics should run. */
	public boolean glide(LivingEntity entity) {
		if (entity != mc.player || mc.player.isClimbing()) return false;

		Vec3d velocity = Vec3d.ZERO;
		if (mc.options.forwardKey.isPressed()) velocity = velocity.add(mc.player.getRotationVec(1f));
		if (mc.options.backKey.isPressed()) velocity = velocity.subtract(mc.player.getRotationVec(1f));
		if (mc.options.jumpKey.isPressed()) velocity = velocity.add(0, 1, 0);
		if (mc.options.sneakKey.isPressed()) velocity = velocity.add(0, -1, 0);
		if (velocity.lengthSquared() > 1.0E-6) velocity = velocity.normalize().multiply(speed.get());
		else velocity = Vec3d.ZERO;

		mc.player.setVelocity(velocity);
		if (velocity.lengthSquared() > 0) mc.player.move(MovementType.SELF, velocity);
		return true;
	}

	@Override
	public String getSuffix() {
		return speed.format();
	}
}
