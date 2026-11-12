package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ColorSetting;
import dev.nova.setting.MultiSetting;
import dev.nova.util.EntityUtil;
import net.minecraft.entity.Entity;

/** The outline is enabled in MinecraftClientMixin and colored in EntityMixin. */
public class GlowESP extends Module {
	private final MultiSetting targets = multi("Цели").add("Игроки", true).add("Мобы", false).add("Животные", false);
	private final ColorSetting color = color("Цвет свечения", 0xFF8B5BFF);

	public GlowESP() {
		super("GlowESP", "Свечение сущностей сквозь стены", Category.VISUALS);
	}

	public boolean shouldGlow(Entity entity) {
		return isEnabled() && EntityUtil.matches(entity, targets);
	}

	public int colorOf() {
		return color.get();
	}
}
