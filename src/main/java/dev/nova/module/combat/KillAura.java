package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class KillAura extends Module {
	private final NumberSetting range = number("Range", 4.0, 2.0, 6.0, 0.1);
	private final BoolSetting players = bool("Players", false);
	private final BoolSetting hostile = bool("Hostile", true);
	private final BoolSetting passive = bool("Passive", false);
	private final BoolSetting rotate = bool("Rotate", false);

	public KillAura() {
		super("KillAura", "Автоматически бьёт ближайшие цели", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player.isDead() || mc.player.isSpectator() || mc.currentScreen != null) return;
		if (mc.player.getAttackCooldownProgress(0.5f) < 1f) return;

		Vec3d eyes = mc.player.getEyePos();
		double best = range.get() * range.get();
		LivingEntity target = null;
		for (Entity entity : mc.world.getEntities()) {
			if (!(entity instanceof LivingEntity living)) continue;
			if (!EntityUtil.matches(entity, players.get(), hostile.get(), passive.get(), false)) continue;
			double distance = squaredDistance(eyes, entity.getBoundingBox());
			if (distance <= best) {
				best = distance;
				target = living;
			}
		}
		if (target == null) return;

		if (rotate.get()) lookAt(eyes, target.getBoundingBox().getCenter());
		mc.interactionManager.attackEntity(mc.player, target);
		mc.player.swingHand(Hand.MAIN_HAND);
	}

	private static double squaredDistance(Vec3d point, Box box) {
		double dx = Math.max(Math.max(box.minX - point.x, 0), point.x - box.maxX);
		double dy = Math.max(Math.max(box.minY - point.y, 0), point.y - box.maxY);
		double dz = Math.max(Math.max(box.minZ - point.z, 0), point.z - box.maxZ);
		return dx * dx + dy * dy + dz * dz;
	}

	private static void lookAt(Vec3d eyes, Vec3d target) {
		Vec3d delta = target.subtract(eyes);
		double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
		mc.player.setYaw((float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90f);
		mc.player.setPitch(MathHelper.clamp((float) -Math.toDegrees(Math.atan2(delta.y, horizontal)), -90f, 90f));
	}

	@Override
	public String getSuffix() {
		return range.format();
	}
}
