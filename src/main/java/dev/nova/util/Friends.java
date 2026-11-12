package dev.nova.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Set;
import java.util.TreeSet;

/** Player names that combat modules leave alone. Persisted by Config. */
public final class Friends {
	public static final Set<String> NAMES = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

	private Friends() {
	}

	public static boolean isFriend(Entity entity) {
		return entity instanceof PlayerEntity player && NAMES.contains(player.getGameProfile().name());
	}

	/** Returns true if the player is a friend after the call. */
	public static boolean toggle(PlayerEntity player) {
		String name = player.getGameProfile().name();
		if (NAMES.remove(name)) return false;
		NAMES.add(name);
		return true;
	}
}
