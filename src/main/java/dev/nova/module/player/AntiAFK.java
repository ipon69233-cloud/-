package dev.nova.module.player;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ModeSetting;
import dev.nova.util.KeyUtil;

public class AntiAFK extends Module {
	private static final int INTERVAL = 100;
	private static final int WALK_TICKS = 6;

	private final ModeSetting action = mode("Действие", "Прыжок", "Прыжок", "Взгляд", "Ходьба");

	private int timer;
	private int walking;
	private boolean turnRight = true;

	public AntiAFK() {
		super("AntiAFK", "Защита от кика за бездействие", Category.PLAYER);
	}

	@Override
	protected void onEnable() {
		timer = INTERVAL;
		walking = 0;
	}

	@Override
	protected void onDisable() {
		if (walking > 0) {
			walking = 0;
			KeyUtil.release(mc.options.forwardKey);
		}
	}

	@Override
	public void onTick() {
		if (walking > 0) {
			if (--walking == 0) KeyUtil.release(mc.options.forwardKey);
			return;
		}
		if (mc.currentScreen != null || --timer > 0) return;
		timer = INTERVAL;

		if (action.is("Прыжок")) {
			if (mc.player.isOnGround()) mc.player.jump();
		} else if (action.is("Взгляд")) {
			mc.player.setYaw(mc.player.getYaw() + (turnRight ? 15f : -15f));
			turnRight = !turnRight;
		} else {
			mc.options.forwardKey.setPressed(true);
			walking = WALK_TICKS;
		}
	}
}
