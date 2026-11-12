package dev.nova.module.player;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * The camera is moved in CameraMixin, mouse look is redirected in EntityMixin
 * and the player stops reading the movement keys in KeyboardInputMixin.
 */
public class FreeCamera extends Module {
	// Blocks per second at a speed setting of 1.
	private static final double UNIT = 10;

	private final NumberSetting speed = number("Скорость", 1.5, 0.5, 5, 0.1);

	private Vec3d pos = Vec3d.ZERO;
	private float yaw;
	private float pitch;
	private long lastFrame;
	private ClientWorld world;

	public FreeCamera() {
		super("FreeCamera", "Свободная камера вокруг игрока", Category.PLAYER);
	}

	@Override
	protected void onEnable() {
		if (inGame()) reset();
	}

	private void reset() {
		pos = mc.player.getEyePos();
		yaw = mc.player.getYaw();
		pitch = mc.player.getPitch();
		lastFrame = System.nanoTime();
		world = mc.world;
	}

	public Vec3d getPos() {
		return pos;
	}

	public float getYaw() {
		return yaw;
	}

	public float getPitch() {
		return pitch;
	}

	/** Mouse movement turns the camera instead of the player. */
	public void rotate(double cursorDeltaX, double cursorDeltaY) {
		yaw += (float) cursorDeltaX * 0.15f;
		pitch = MathHelper.clamp(pitch + (float) cursorDeltaY * 0.15f, -90f, 90f);
	}

	/** Called once per frame before the camera is placed. */
	public void step() {
		if (mc.player == null || mc.world == null) return;
		if (world != mc.world) reset();

		long now = System.nanoTime();
		double seconds = Math.min((now - lastFrame) / 1.0e9, 0.1);
		lastFrame = now;
		if (mc.currentScreen != null) return;

		Vec3d forward = Vec3d.fromPolar(pitch, yaw);
		double radians = Math.toRadians(yaw);
		Vec3d left = new Vec3d(Math.cos(radians), 0, Math.sin(radians));
		Vec3d move = Vec3d.ZERO;
		if (mc.options.forwardKey.isPressed()) move = move.add(forward);
		if (mc.options.backKey.isPressed()) move = move.subtract(forward);
		if (mc.options.leftKey.isPressed()) move = move.add(left);
		if (mc.options.rightKey.isPressed()) move = move.subtract(left);
		if (mc.options.jumpKey.isPressed()) move = move.add(0, 1, 0);
		if (mc.options.sneakKey.isPressed()) move = move.subtract(0, 1, 0);
		if (move.lengthSquared() > 1.0E-6) pos = pos.add(move.normalize().multiply(speed.get() * UNIT * seconds));
	}
}
