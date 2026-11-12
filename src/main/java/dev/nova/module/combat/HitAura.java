package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ModeSetting;
import dev.nova.setting.MultiSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.util.EntityUtil;
import dev.nova.util.Friends;
import dev.nova.util.InvUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class HitAura extends Module {
	private static final float SMOOTH_STEP = 25f;
	// Smooth rotation hits only once it is this close to the target.
	private static final float SMOOTH_AIM_ERROR = 12f;
	private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private final NumberSetting radius = number("Радиус атаки", 3.5, 3.0, 6.0, 0.1);
	private final ModeSetting rotation = mode("Режим ротации", "Smooth", "Smooth", "Snap", "Нет");
	private final NumberSetting fov = number("FOV", 180, 5, 180, 1);
	private final MultiSetting targets = multi("Цели")
		.add("Игроки", true).add("Голые", true).add("Невидимки", true).add("Друзья", false).add("Мобы", true).add("Животные", false);
	private final ModeSetting timing = mode("Тайминг удара", "Быстрый", "Быстрый", "Динамичный");
	private final MultiSetting checks = multi("Проверки")
		.add("Через блоки", false).add("Только оружие", false).add("Не бить при еде", true).add("Ломать щит", false);

	private LivingEntity target;

	public HitAura() {
		super("HitAura", "Автоматически атакует ближайших врагов в радиусе", Category.COMBAT);
	}

	/** Current target, or null when there is none or the module is off. */
	public LivingEntity getTarget() {
		return target;
	}

	@Override
	protected void onDisable() {
		target = null;
	}

	@Override
	public void onTick() {
		target = null;
		if (mc.player.isDead() || mc.player.isSpectator() || mc.currentScreen != null) return;
		if (checks.is("Не бить при еде") && mc.player.isUsingItem()) return;
		if (checks.is("Только оружие") && !isWeapon(mc.player.getMainHandStack())) return;

		target = findTarget();
		if (target == null) return;

		Vec3d eyes = mc.player.getEyePos();
		Vec3d aim = target.getBoundingBox().getCenter();
		boolean smooth = rotation.is("Smooth");
		if (!rotation.is("Нет")) rotate(eyes, aim, smooth);
		if (smooth && angleTo(eyes, aim) > SMOOTH_AIM_ERROR) return;
		if (!canHit()) return;

		attack(target);
	}

	private LivingEntity findTarget() {
		Vec3d eyes = mc.player.getEyePos();
		double best = radius.get() * radius.get();
		LivingEntity found = null;
		for (Entity entity : mc.world.getEntities()) {
			if (!(entity instanceof LivingEntity living) || !isValid(living)) continue;
			double distance = squaredDistance(eyes, living.getBoundingBox());
			if (distance > best) continue;
			if (fov.get() < 180 && angleTo(eyes, living.getBoundingBox().getCenter()) > fov.get()) continue;
			if (!checks.is("Через блоки") && !mc.player.canSee(living)) continue;
			best = distance;
			found = living;
		}
		return found;
	}

	private boolean isValid(LivingEntity entity) {
		if (entity == mc.player || !entity.isAlive()) return false;
		if (entity.isInvisible() && !targets.is("Невидимки")) return false;
		if (entity instanceof PlayerEntity player) {
			if (Friends.isFriend(player)) return targets.is("Друзья");
			if (!targets.is("Игроки")) return false;
			return targets.is("Голые") || !isNaked(player);
		}
		return switch (EntityUtil.kindOf(entity)) {
			case HOSTILE -> targets.is("Мобы");
			case PASSIVE -> targets.is("Животные");
			default -> false;
		};
	}

	private static boolean isNaked(PlayerEntity player) {
		for (EquipmentSlot slot : ARMOR) {
			if (!player.getEquippedStack(slot).isEmpty()) return false;
		}
		return true;
	}

	private static boolean isWeapon(ItemStack stack) {
		return stack.isIn(ItemTags.SWORDS) || stack.isIn(ItemTags.AXES) || stack.isIn(ItemTags.SPEARS)
			|| stack.isOf(Items.MACE) || stack.isOf(Items.TRIDENT);
	}

	private boolean canHit() {
		if (mc.player.getAttackCooldownProgress(0.5f) < 1f) return false;
		if (!timing.is("Динамичный")) return true;
		boolean airborne = !mc.player.isOnGround() && !mc.player.isTouchingWater() && !mc.player.isInLava()
			&& !mc.player.isClimbing() && !mc.player.getAbilities().flying;
		// A critical hit needs the player to be falling.
		return !airborne || (mc.player.fallDistance > 0f && mc.player.getVelocity().y < 0);
	}

	private void attack(LivingEntity entity) {
		PlayerInventory inventory = mc.player.getInventory();
		int previous = inventory.getSelectedSlot();
		int axe = -1;
		if (checks.is("Ломать щит") && entity instanceof PlayerEntity player && player.isBlocking() && !mc.player.getMainHandStack().isIn(ItemTags.AXES)) {
			axe = InvUtil.findHotbar(stack -> stack.isIn(ItemTags.AXES));
		}
		if (axe != -1) inventory.setSelectedSlot(axe);
		mc.interactionManager.attackEntity(mc.player, entity);
		mc.player.swingHand(Hand.MAIN_HAND);
		if (axe != -1) inventory.setSelectedSlot(previous);
	}

	private static void rotate(Vec3d eyes, Vec3d aim, boolean smooth) {
		Vec3d delta = aim.subtract(eyes);
		float yaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90f;
		float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)));
		float yawDelta = MathHelper.wrapDegrees(yaw - mc.player.getYaw());
		float pitchDelta = pitch - mc.player.getPitch();
		if (smooth) {
			yawDelta = MathHelper.clamp(yawDelta, -SMOOTH_STEP, SMOOTH_STEP);
			pitchDelta = MathHelper.clamp(pitchDelta, -SMOOTH_STEP, SMOOTH_STEP);
		}
		mc.player.setYaw(mc.player.getYaw() + yawDelta);
		mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + pitchDelta, -90f, 90f));
	}

	/** Angle in degrees between the view direction and the direction to the point. */
	private static double angleTo(Vec3d eyes, Vec3d point) {
		Vec3d direction = point.subtract(eyes).normalize();
		double dot = MathHelper.clamp(mc.player.getRotationVec(1f).dotProduct(direction), -1.0, 1.0);
		return Math.toDegrees(Math.acos(dot));
	}

	private static double squaredDistance(Vec3d point, Box box) {
		double dx = Math.max(Math.max(box.minX - point.x, 0), point.x - box.maxX);
		double dy = Math.max(Math.max(box.minY - point.y, 0), point.y - box.maxY);
		double dz = Math.max(Math.max(box.minZ - point.z, 0), point.z - box.maxZ);
		return dx * dx + dy * dy + dz * dz;
	}

	@Override
	public String getSuffix() {
		return radius.format();
	}
}
