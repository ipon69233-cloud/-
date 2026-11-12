package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import dev.nova.util.MoveUtil;
import net.minecraft.util.math.Vec3d;

public class WaterSpeed extends Module {
	// Vanilla horizontal speed while wading and while swimming, in blocks per tick.
	private static final double WADING = 0.11;
	private static final double SWIMMING = 0.2;

	private final NumberSetting speed = number("Скорость", 1.4, 1.0, 3.0, 0.1);

	public WaterSpeed() {
		super("WaterSpeed", "Ускорение передвижения под водой", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (!mc.player.isTouchingWater() || mc.player.getAbilities().flying || mc.player.isGliding() || mc.player.hasVehicle()) return;

		Vec3d direction = MoveUtil.direction();
		if (direction == null) return;
		double target = (mc.player.isSwimming() ? SWIMMING : WADING) * speed.get();
		mc.player.setVelocity(direction.x * target, mc.player.getVelocity().y, direction.z * target);
	}

	@Override
	public String getSuffix() {
		return speed.format();
	}
}
