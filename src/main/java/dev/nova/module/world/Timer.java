package dev.nova.module.world;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;

/** The client tick rate is scaled in RenderTickCounterMixin. */
public class Timer extends Module {
	private final NumberSetting speed = number("Speed", 2.0, 0.1, 5.0, 0.1);

	public Timer() {
		super("Timer", "Ускоряет или замедляет игру для игрока", Category.MISC);
	}

	public float getMultiplier() {
		return speed.getFloat();
	}

	@Override
	public String getSuffix() {
		return speed.format();
	}
}
