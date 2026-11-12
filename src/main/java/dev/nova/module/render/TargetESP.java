package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.module.Modules;
import dev.nova.setting.ColorSetting;
import dev.nova.util.WorldOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class TargetESP extends Module {
	private static final int TRAIL_RINGS = 5;

	private final ColorSetting color = color("Цвет", 0xFFFF4D4D);

	public TargetESP() {
		super("TargetESP", "Выделение текущей цели", Category.VISUALS);
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		LivingEntity target = Modules.HIT_AURA.getTarget();
		if (target == null || !target.isAlive()) return;

		Vec3d base = target.getLerpedPos(tickDelta);
		double height = target.getHeight();
		double radius = target.getWidth() * 0.5 + 0.25;
		float time = mc.world.getTime() + tickDelta;

		WorldOverlay.Lines lines = WorldOverlay.begin(context);
		for (int i = 0; i < TRAIL_RINGS; i++) {
			// Each trailing ring lags a little behind the leading one and fades out.
			double phase = (time - i * 1.5) * 0.15;
			double y = base.y + (Math.sin(phase) * 0.5 + 0.5) * height;
			int alpha = 255 - i * 45;
			lines.circle(new Vec3d(base.x, y, base.z), radius, 24, color.withAlpha(alpha), 2f);
		}
		lines.submit(context);
	}
}
