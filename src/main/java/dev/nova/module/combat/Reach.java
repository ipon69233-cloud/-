package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import net.minecraft.entity.attribute.EntityAttributes;

public class Reach extends Module {
	// The server tolerates +3 blocks for entities and +1 for blocks on top of the vanilla range.
	private final NumberSetting entities = number("Сущности", 5.0, 3.0, 6.0, 0.1);
	private final NumberSetting blocks = number("Блоки", 5.5, 4.5, 5.5, 0.1);

	public Reach() {
		super("Reach", "Увеличивает дистанцию ударов и взаимодействия", Category.COMBAT);
	}

	@Override
	public void onTick() {
		setAttribute(EntityAttributes.ENTITY_INTERACTION_RANGE, entities.get());
		setAttribute(EntityAttributes.BLOCK_INTERACTION_RANGE, blocks.get());
	}

	@Override
	protected void onDisable() {
		if (!inGame()) return;
		setAttribute(EntityAttributes.ENTITY_INTERACTION_RANGE, 3.0);
		setAttribute(EntityAttributes.BLOCK_INTERACTION_RANGE, 4.5);
	}

	@Override
	public String getSuffix() {
		return entities.format();
	}
}
