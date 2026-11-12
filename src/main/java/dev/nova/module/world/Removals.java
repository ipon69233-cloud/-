package dev.nova.module.world;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.MultiSetting;

/** The effects are skipped in GameRendererMixin, InGameHudMixin and InGameOverlayRendererMixin. */
public class Removals extends Module {
	private final MultiSetting remove = multi("Убрать")
		.add("Тряску", true).add("Огонь на экране", true).add("Тыкву", false).add("Воду", false);

	public Removals() {
		super("Removals", "Убирает эффекты и оверлеи", Category.MISC);
	}

	public boolean removes(String item) {
		return isEnabled() && remove.is(item);
	}
}
