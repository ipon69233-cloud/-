package dev.nova.module.player;

import dev.nova.mixin.LivingEntityAccessor;
import dev.nova.mixin.MinecraftClientAccessor;
import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.MultiSetting;

public class NoDelay extends Module {
	private final MultiSetting delays = multi("Задержки").add("Прыжок", true).add("Правый клик", true);

	public NoDelay() {
		super("NoDelay", "Убирает задержки действий", Category.PLAYER);
	}

	@Override
	public void onTick() {
		if (delays.is("Прыжок")) ((LivingEntityAccessor) mc.player).nova$setJumpingCooldown(0);
		if (delays.is("Правый клик")) ((MinecraftClientAccessor) mc).nova$setItemUseCooldown(0);
	}
}
