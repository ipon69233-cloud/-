package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ColorSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.util.WorldOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

public class ChinaHat extends Module {
	private static final int SEGMENTS = 24;

	private final NumberSetting size = number("Размер", 1, 0.3, 3, 0.1);
	private final ColorSetting color = color("Цвет", 0xFFC05BFF);

	public ChinaHat() {
		super("ChinaHat", "Вращающийся конус над игроком", Category.VISUALS);
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		if (!mc.gameRenderer.getCamera().isThirdPerson()) return;

		Vec3d head = mc.player.getLerpedPos(tickDelta).add(0, mc.player.getHeight() - 0.02, 0);
		double radius = 0.5 * size.get();
		Vec3d apex = head.add(0, 0.4 * size.get(), 0);
		double spin = (mc.world.getTime() + tickDelta) * 0.08;

		WorldOverlay.Lines lines = WorldOverlay.begin(context);
		lines.circle(head, radius, SEGMENTS, color.get(), 1.5f);
		for (int i = 0; i < SEGMENTS; i += 2) {
			double angle = spin + Math.PI * 2 * i / SEGMENTS;
			Vec3d rim = new Vec3d(head.x + Math.cos(angle) * radius, head.y, head.z + Math.sin(angle) * radius);
			lines.line(rim, apex, color.withAlpha(200), 1.5f);
		}
		lines.submit(context);
	}
}
