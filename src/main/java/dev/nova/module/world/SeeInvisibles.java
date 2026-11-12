package dev.nova.module.world;

import dev.nova.module.Category;
import dev.nova.module.Module;

/** Invisible entities are shown as translucent ghosts, see EntityMixin. */
public class SeeInvisibles extends Module {
	public SeeInvisibles() {
		super("SeeInvisibles", "Показывает невидимых игроков", Category.MISC);
	}
}
