package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import net.minecraft.entity.attribute.EntityAttributes;

public class Step extends Module {
	private final NumberSetting height = number("Высота", 1.5, 1.0, 5.0, 0.5);

	public Step() {
		super("Step", "Позволяет заходить на высокие блоки без прыжка", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		setAttribute(EntityAttributes.STEP_HEIGHT, height.get());
	}

	@Override
	protected void onDisable() {
		if (inGame()) setAttribute(EntityAttributes.STEP_HEIGHT, 0.6);
	}

	@Override
	public String getSuffix() {
		return height.format();
	}
}
