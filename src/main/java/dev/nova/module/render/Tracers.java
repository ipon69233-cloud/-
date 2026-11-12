package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ColorSetting;
import dev.nova.setting.MultiSetting;
import dev.nova.util.EntityUtil;
import dev.nova.util.Friends;
import dev.nova.util.WorldOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class Tracers extends Module {
	private final ColorSetting color = color("Цвет", 0xFFFFFFFF);
	private final MultiSetting targets = multi("Цели").add("Игроки", true).add("Мобы", true).add("Животные", false).add("Предметы", false);

	public Tracers() {
		super("Tracers", "Рисует линии от прицела к целям", Category.VISUALS);
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		WorldOverlay.Lines lines = WorldOverlay.begin(context);
		for (Entity entity : mc.world.getEntities()) {
			if (!EntityUtil.matches(entity, targets)) continue;
			int lineColor = Friends.isFriend(entity) ? ESP.FRIEND_COLOR : entity instanceof PlayerEntity ? color.get() : EntityUtil.kindOf(entity).color;
			lines.tracer(EntityUtil.lerpedBox(entity, tickDelta).getCenter(), lineColor, 1.5f);
		}
		lines.submit(context);
	}
}
