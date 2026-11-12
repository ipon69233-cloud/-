package dev.nova.module.world;

import dev.nova.mixin.KeyBindingAccessor;
import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ModeSetting;
import dev.nova.util.KeyUtil;
import net.minecraft.util.hit.HitResult;

public class TapeMouse extends Module {
	private final ModeSetting button = mode("Кнопка", "ЛКМ", "ЛКМ", "ПКМ");

	public TapeMouse() {
		super("TapeMouse", "Зажимает кнопку мыши", Category.MISC);
	}

	@Override
	protected void onDisable() {
		if (!inGame()) return;
		KeyUtil.release(mc.options.attackKey);
		KeyUtil.release(mc.options.useKey);
	}

	@Override
	public void onTick() {
		if (mc.currentScreen != null) return;
		if (button.is("ПКМ")) {
			KeyUtil.release(mc.options.attackKey);
			mc.options.useKey.setPressed(true);
			return;
		}

		KeyUtil.release(mc.options.useKey);
		mc.options.attackKey.setPressed(true);
		// Holding the key only repeats block breaking, attacks need a fresh click.
		if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY && mc.player.getAttackCooldownProgress(0.5f) >= 1f) {
			((KeyBindingAccessor) mc.options.attackKey).nova$setTimesPressed(1);
		}
	}

	@Override
	public String getSuffix() {
		return button.get();
	}
}
