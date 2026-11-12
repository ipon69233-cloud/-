package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import net.minecraft.entity.attribute.EntityAttributes;

public class HighJump extends Module {
	private static final double DEFAULT_JUMP_STRENGTH = 0.42;

	private final NumberSetting multiplier = number("Множитель", 2.0, 1.0, 8.0, 0.5);

	public HighJump() {
		super("HighJump", "Увеличивает высоту прыжка", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		setAttribute(EntityAttributes.JUMP_STRENGTH, DEFAULT_JUMP_STRENGTH * multiplier.get());
	}

	@Override
	protected void onDisable() {
		if (inGame()) setAttribute(EntityAttributes.JUMP_STRENGTH, DEFAULT_JUMP_STRENGTH);
	}

	@Override
	public String getSuffix() {
		return multiplier.format();
	}
}
