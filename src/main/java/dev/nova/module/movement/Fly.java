package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import net.minecraft.entity.player.PlayerAbilities;

public class Fly extends Module {
	private static final float DEFAULT_FLY_SPEED = 0.05f;

	private final NumberSetting speed = number("Скорость", 2.0, 0.5, 10.0, 0.5);

	public Fly() {
		super("Fly", "Полёт как в креативе в любом режиме", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		PlayerAbilities abilities = mc.player.getAbilities();
		// The game clears the flag on landing, so keep it set.
		abilities.flying = true;
		abilities.setFlySpeed(DEFAULT_FLY_SPEED * speed.getFloat());
	}

	@Override
	protected void onDisable() {
		if (!inGame()) return;
		PlayerAbilities abilities = mc.player.getAbilities();
		abilities.setFlySpeed(DEFAULT_FLY_SPEED);
		if (!abilities.allowFlying) abilities.flying = false;
	}

	@Override
	public String getSuffix() {
		return speed.format();
	}
}
