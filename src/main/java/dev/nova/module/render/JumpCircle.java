package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ColorSetting;
import dev.nova.util.WorldOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class JumpCircle extends Module {
	private static final float LIFETIME = 20f;
	private static final double MAX_RADIUS = 1.6;

	private record Ring(Vec3d pos, long start) {
	}

	private final ColorSetting color = color("Цвет", 0xFF8B5BFF);

	private final List<Ring> rings = new ArrayList<>();
	private boolean wasOnGround;

	public JumpCircle() {
		super("JumpCircle", "Круг под ногами при прыжке", Category.VISUALS);
	}

	@Override
	protected void onEnable() {
		wasOnGround = true;
	}

	@Override
	protected void onDisable() {
		rings.clear();
	}

	@Override
	public void onTick() {
		boolean onGround = mc.player.isOnGround();
		if (wasOnGround && !onGround && mc.player.getVelocity().y > 0.1) {
			rings.add(new Ring(mc.player.getEntityPos().add(0, 0.02, 0), mc.world.getTime()));
		}
		wasOnGround = onGround;
		rings.removeIf(ring -> mc.world.getTime() - ring.start > LIFETIME);
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		if (rings.isEmpty()) return;
		WorldOverlay.Lines lines = WorldOverlay.begin(context);
		for (Ring ring : rings) {
			float progress = Math.min(1f, (mc.world.getTime() - ring.start + tickDelta) / LIFETIME);
			// Expands quickly at first and fades out towards the end.
			double radius = 0.2 + MAX_RADIUS * (1 - Math.pow(1 - progress, 2));
			lines.circle(ring.pos, radius, 32, color.withAlpha((int) (255 * (1 - progress))), 2f);
		}
		lines.submit(context);
	}
}
