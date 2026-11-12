package dev.nova.util;

import dev.nova.setting.MultiSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class EntityUtil {
	public enum Kind {
		PLAYER(0xFFFF5555),
		HOSTILE(0xFFFFAA00),
		PASSIVE(0xFF55FF55),
		ITEM(0xFFFFFF55),
		OTHER(0xFFAAAAAA);

		public final int color;

		Kind(int color) {
			this.color = color;
		}
	}

	private EntityUtil() {
	}

	public static Kind kindOf(Entity entity) {
		if (entity instanceof PlayerEntity) return Kind.PLAYER;
		if (entity instanceof Monster) return Kind.HOSTILE;
		if (entity instanceof ItemEntity) return Kind.ITEM;
		if (entity instanceof LivingEntity && !(entity instanceof ArmorStandEntity)) return Kind.PASSIVE;
		return Kind.OTHER;
	}

	public static boolean matches(Entity entity, boolean players, boolean hostile, boolean passive, boolean items) {
		if (entity == MinecraftClient.getInstance().player || !entity.isAlive()) return false;
		return switch (kindOf(entity)) {
			case PLAYER -> players;
			case HOSTILE -> hostile;
			case PASSIVE -> passive;
			case ITEM -> items;
			case OTHER -> false;
		};
	}

	/** Filter for the usual Игроки / Мобы / Животные / Предметы target settings. */
	public static boolean matches(Entity entity, MultiSetting targets) {
		return matches(entity, targets.is("Игроки"), targets.is("Мобы"), targets.is("Животные"), targets.is("Предметы"));
	}

	/** Bounding box at the interpolated position of the current frame. */
	public static Box lerpedBox(Entity entity, float tickDelta) {
		Vec3d offset = entity.getLerpedPos(tickDelta).subtract(entity.getEntityPos());
		return entity.getBoundingBox().offset(offset);
	}
}
