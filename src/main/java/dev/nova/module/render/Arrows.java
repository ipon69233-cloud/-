package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ColorSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.util.WorldOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Arrows extends Module {
	private final NumberSetting radius = number("Радиус", 60, 30, 120, 5);
	private final ColorSetting color = color("Цвет", 0xFFFF4D4D);

	public Arrows() {
		super("Arrows", "Указатели-стрелки на врагов", Category.VISUALS);
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		float centerX = context.getScaledWindowWidth() / 2f;
		float centerY = context.getScaledWindowHeight() / 2f;
		float viewYaw = mc.gameRenderer.getCamera().getYaw();
		Vec3d own = mc.player.getLerpedPos(tickDelta);

		WorldOverlay.Lines lines = WorldOverlay.begin(context);
		for (PlayerEntity player : mc.world.getPlayers()) {
			if (player == mc.player || !player.isAlive()) continue;
			Vec3d delta = player.getLerpedPos(tickDelta).subtract(own);
			float targetYaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90f;
			// Positive means the target is to the right of the view direction.
			float relative = (float) Math.toRadians(MathHelper.wrapDegrees(targetYaw - viewYaw));
			float dirX = (float) Math.sin(relative);
			float dirY = (float) -Math.cos(relative);
			float x = centerX + dirX * radius.getFloat();
			float y = centerY + dirY * radius.getFloat();
			// Perpendicular of the direction, used for the base of the triangle.
			float sideX = -dirY;
			float sideY = dirX;
			lines.triangle(
				x + dirX * 6, y + dirY * 6,
				x - dirX * 4 + sideX * 5, y - dirY * 4 + sideY * 5,
				x - dirX * 4 - sideX * 5, y - dirY * 4 - sideY * 5,
				color.get()
			);
		}
		lines.submit(context);
	}
}
