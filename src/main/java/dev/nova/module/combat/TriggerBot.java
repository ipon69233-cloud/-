package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import dev.nova.util.EntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class TriggerBot extends Module {
	private final BoolSetting players = bool("Players", true);
	private final BoolSetting hostile = bool("Hostile", true);
	private final BoolSetting passive = bool("Passive", false);

	public TriggerBot() {
		super("TriggerBot", "Бьёт цель, когда на неё наведён прицел", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.currentScreen != null || !(mc.crosshairTarget instanceof EntityHitResult hit)) return;
		if (!(hit.getEntity() instanceof LivingEntity target)) return;
		if (!EntityUtil.matches(target, players.get(), hostile.get(), passive.get(), false)) return;
		if (mc.player.getAttackCooldownProgress(0.5f) < 1f) return;

		mc.interactionManager.attackEntity(mc.player, target);
		mc.player.swingHand(Hand.MAIN_HAND);
	}
}
