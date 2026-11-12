package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/** The fluid surface becomes solid through LivingEntityMixin. */
public class Jesus extends Module {
	public Jesus() {
		super("Jesus", "Ходьба по воде и лаве (Shift — нырнуть)", Category.MOVEMENT);
	}

	public boolean shouldWalkOnFluid(LivingEntity entity) {
		return entity == mc.player && !mc.options.sneakKey.isPressed();
	}

	@Override
	public void onTick() {
		if (mc.options.sneakKey.isPressed() || mc.player.isOnGround() || mc.player.getAbilities().flying) return;
		if (!mc.player.isTouchingWater() && !mc.player.isInLava()) return;
		// Float back up to the surface after falling in.
		Vec3d velocity = mc.player.getVelocity();
		if (velocity.y < 0.11) mc.player.setVelocity(velocity.x, 0.11, velocity.z);
	}
}
