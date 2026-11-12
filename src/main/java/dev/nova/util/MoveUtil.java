package dev.nova.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public final class MoveUtil {
	private static final MinecraftClient mc = MinecraftClient.getInstance();

	private MoveUtil() {
	}

	private static float axis(boolean positive, boolean negative) {
		return (positive ? 1 : 0) - (negative ? 1 : 0);
	}

	public static boolean isMoving() {
		return mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed()
			|| mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
	}

	/** Horizontal unit vector of the movement keys relative to the view, or null when none is held. */
	public static Vec3d direction() {
		float forward = axis(mc.options.forwardKey.isPressed(), mc.options.backKey.isPressed());
		float sideways = axis(mc.options.leftKey.isPressed(), mc.options.rightKey.isPressed());
		if (forward == 0 && sideways == 0) return null;

		double yaw = Math.toRadians(mc.player.getYaw());
		double sin = Math.sin(yaw);
		double cos = Math.cos(yaw);
		// Forward is (-sin, cos) and left is (cos, sin) in Minecraft's yaw convention.
		return new Vec3d(forward * -sin + sideways * cos, 0, forward * cos + sideways * sin).normalize();
	}
}
