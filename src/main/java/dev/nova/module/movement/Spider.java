package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

public class Spider extends Module {
	private final NumberSetting speed = number("Скорость", 0.2, 0.1, 1.0, 0.05);

	public Spider() {
		super("Spider", "Лазание по вертикальным стенам", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (!mc.player.horizontalCollision || mc.player.getAbilities().flying) return;
		Vec3d velocity = mc.player.getVelocity();
		if (velocity.y < speed.get()) mc.player.setVelocity(velocity.x, speed.get(), velocity.z);
	}
}
