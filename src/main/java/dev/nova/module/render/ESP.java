package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import dev.nova.setting.ModeSetting;
import dev.nova.util.EntityUtil;
import dev.nova.util.WorldOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;

public class ESP extends Module {
	private final ModeSetting mode = mode("Mode", "Glow", "Glow", "Box", "Both");
	private final BoolSetting players = bool("Players", true);
	private final BoolSetting hostile = bool("Hostile", true);
	private final BoolSetting passive = bool("Passive", false);
	private final BoolSetting items = bool("Items", false);

	public ESP() {
		super("ESP", "Подсвечивает игроков и мобов сквозь стены", Category.RENDER);
	}

	private boolean matches(Entity entity) {
		return EntityUtil.matches(entity, players.get(), hostile.get(), passive.get(), items.get());
	}

	/** Used by the outline mixins. */
	public boolean shouldGlow(Entity entity) {
		return isEnabled() && !mode.is("Box") && matches(entity);
	}

	public int colorOf(Entity entity) {
		return EntityUtil.kindOf(entity).color;
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		if (mode.is("Glow")) return;
		WorldOverlay.Lines lines = WorldOverlay.begin(context);
		for (Entity entity : mc.world.getEntities()) {
			if (matches(entity)) lines.box(EntityUtil.lerpedBox(entity, tickDelta), colorOf(entity), 1.5f);
		}
		lines.submit(context);
	}

	@Override
	public String getSuffix() {
		return mode.get();
	}
}
