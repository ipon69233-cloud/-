package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

/** The packet handling lives in ClientPlayNetworkHandlerMixin. */
public class Velocity extends Module {
	private final NumberSetting horizontal = number("Горизонталь", 0, 0, 100, 1);
	private final NumberSetting vertical = number("Вертикаль", 0, 0, 100, 1);

	public Velocity() {
		super("Velocity", "Уменьшает отбрасывание от ударов", Category.COMBAT);
	}

	/** Knockback delivered on top of the current velocity, scaled by the configured percentages. */
	public Vec3d scaleKnockback(Vec3d knockback) {
		return new Vec3d(knockback.x * horizontal.get() / 100, knockback.y * vertical.get() / 100, knockback.z * horizontal.get() / 100);
	}

	/** New velocity after a server velocity packet: the part added to the current velocity is scaled. */
	public Vec3d scaleVelocity(Vec3d current, Vec3d packet) {
		return current.add(scaleKnockback(packet.subtract(current)));
	}

	@Override
	public String getSuffix() {
		return horizontal.getInt() + "% " + vertical.getInt() + "%";
	}
}
