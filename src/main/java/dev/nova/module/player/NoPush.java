package dev.nova.module.player;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.MultiSetting;

/** The pushes are cancelled in EntityMixin, PlayerEntityMixin and ClientPlayerEntityMixin. */
public class NoPush extends Module {
	private final MultiSetting sources = multi("Источник").add("Игроки", true).add("Вода", true).add("Блоки", true);

	public NoPush() {
		super("NoPush", "Отключает толчки от сущностей", Category.PLAYER);
	}

	public boolean ignoresEntities() {
		return sources.is("Игроки");
	}

	public boolean ignoresFluids() {
		return sources.is("Вода");
	}

	public boolean ignoresBlocks() {
		return sources.is("Блоки");
	}
}
