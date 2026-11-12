package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ModeSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/** In the solid mode the fluid surface becomes solid through LivingEntityMixin. */
public class Jesus extends Module {
	private final ModeSetting mode = mode("Режим", "Твёрдая вода", "Твёрдая вода", "Дельфин");

	public Jesus() {
		super("Jesus", "Позволяет ходить по воде", Category.MOVEMENT);
	}

	public boolean shouldWalkOnFluid(LivingEntity entity) {
		return entity == mc.player && mode.is("Твёрдая вода") && !mc.options.sneakKey.isPressed();
	}

	@Override
	public void onTick() {
		if (mc.options.sneakKey.isPressed() || mc.player.isOnGround() || mc.player.getAbilities().flying) return;
		if (!mc.player.isTouchingWater() && !mc.player.isInLava()) return;
		// Solid mode floats back up to the surface, dolphin mode keeps pushing up and bobs over it.
		double lift = mode.is("Дельфин") ? 0.2 : 0.11;
		Vec3d velocity = mc.player.getVelocity();
		if (velocity.y < lift) mc.player.setVelocity(velocity.x, lift, velocity.z);
	}

	@Override
	public String getSuffix() {
		return mode.get();
	}
}
