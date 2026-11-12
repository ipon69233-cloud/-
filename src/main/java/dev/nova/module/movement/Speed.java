package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ModeSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.util.MoveUtil;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
	private final ModeSetting mode = mode("Режим", "Strafe", "Strafe", "Bhop");
	private final NumberSetting speed = number("Скорость", 0.6, 0.3, 3.0, 0.05);

	public Speed() {
		super("Speed", "Ускоряет передвижение игрока", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (mc.player.getAbilities().flying || mc.player.isGliding() || mc.player.hasVehicle()) return;

		Vec3d direction = MoveUtil.direction();
		if (direction == null) return;

		if (mode.is("Bhop") && mc.player.isOnGround() && !mc.player.isSneaking() && !mc.player.isTouchingWater() && !mc.player.isInLava()) {
			mc.player.jump();
		}
		mc.player.setVelocity(direction.x * speed.get(), mc.player.getVelocity().y, direction.z * speed.get());
	}

	@Override
	public String getSuffix() {
		return mode.get();
	}
}
