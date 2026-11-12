package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/** The margin is added in EntityMixin, it only widens what the crosshair can pick. */
public class HitBox extends Module {
	private final NumberSetting size = number("Размер", 0.3, 0, 1, 0.05);

	public HitBox() {
		super("HitBox", "Расширяет хитбокс цели", Category.COMBAT);
	}

	public float margin(Entity entity) {
		return entity instanceof LivingEntity && entity != mc.player ? size.getFloat() : 0f;
	}

	@Override
	public String getSuffix() {
		return size.format();
	}
}
