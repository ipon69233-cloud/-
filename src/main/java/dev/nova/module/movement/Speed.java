package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
	private final NumberSetting speed = number("Speed", 0.6, 0.3, 3.0, 0.05);

	public Speed() {
		super("Speed", "Ускоряет передвижение по земле и в воздухе", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (mc.player.getAbilities().flying || mc.player.isGliding() || mc.player.hasVehicle()) return;

		float forward = axis(mc.options.forwardKey.isPressed(), mc.options.backKey.isPressed());
		float sideways = axis(mc.options.leftKey.isPressed(), mc.options.rightKey.isPressed());
		if (forward == 0 && sideways == 0) return;

		double yaw = Math.toRadians(mc.player.getYaw());
		double sin = Math.sin(yaw);
		double cos = Math.cos(yaw);
		// Forward is (-sin, cos) and left is (cos, sin) in Minecraft's yaw convention.
		Vec3d direction = new Vec3d(forward * -sin + sideways * cos, 0, forward * cos + sideways * sin).normalize();
		mc.player.setVelocity(direction.x * speed.get(), mc.player.getVelocity().y, direction.z * speed.get());
	}

	private static float axis(boolean positive, boolean negative) {
		return (positive ? 1 : 0) - (negative ? 1 : 0);
	}

	@Override
	public String getSuffix() {
		return speed.format();
	}
}
