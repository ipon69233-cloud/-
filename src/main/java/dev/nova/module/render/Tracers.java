package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import dev.nova.util.EntityUtil;
import dev.nova.util.WorldOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;

public class Tracers extends Module {
	private final BoolSetting players = bool("Players", true);
	private final BoolSetting hostile = bool("Hostile", true);
	private final BoolSetting passive = bool("Passive", false);
	private final BoolSetting items = bool("Items", false);

	public Tracers() {
		super("Tracers", "Рисует линии от прицела к целям", Category.VISUALS);
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		WorldOverlay.Lines lines = WorldOverlay.begin(context);
		for (Entity entity : mc.world.getEntities()) {
			if (!EntityUtil.matches(entity, players.get(), hostile.get(), passive.get(), items.get())) continue;
			lines.tracer(EntityUtil.lerpedBox(entity, tickDelta).getCenter(), EntityUtil.kindOf(entity).color, 1.5f);
		}
		lines.submit(context);
	}
}
