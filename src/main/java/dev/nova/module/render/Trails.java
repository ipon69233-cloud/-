package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ColorSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.util.WorldOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class Trails extends Module {
	private static final double TELEPORT_DISTANCE = 16;

	private final NumberSetting length = number("Длина", 20, 5, 60, 1);
	private final ColorSetting color = color("Цвет", 0xFF20D0E0);

	private final Deque<Vec3d> points = new ArrayDeque<>();

	public Trails() {
		super("Trails", "Оставляет след за игроком", Category.VISUALS);
	}

	@Override
	protected void onEnable() {
		points.clear();
	}

	@Override
	protected void onDisable() {
		points.clear();
	}

	@Override
	public void onTick() {
		Vec3d position = mc.player.getEntityPos().add(0, 0.1, 0);
		if (!points.isEmpty() && points.peekLast().squaredDistanceTo(position) > TELEPORT_DISTANCE * TELEPORT_DISTANCE) points.clear();
		points.addLast(position);
		while (points.size() > length.getInt()) points.removeFirst();
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		if (points.isEmpty() || !mc.gameRenderer.getCamera().isThirdPerson()) return;

		WorldOverlay.Lines lines = WorldOverlay.begin(context);
		int count = points.size();
		int index = 0;
		Vec3d previous = null;
		Iterator<Vec3d> iterator = points.iterator();
		while (iterator.hasNext()) {
			Vec3d point = iterator.next();
			if (previous != null) {
				int alpha = (int) (255.0 * index / count);
				lines.line(previous, point, color.withAlpha(alpha), 2f);
			}
			previous = point;
			index++;
		}
		// The newest segment follows the interpolated position so the trail has no gap at the feet.
		lines.line(previous, mc.player.getLerpedPos(tickDelta).add(0, 0.1, 0), color.get(), 2f);
		lines.submit(context);
	}

	@Override
	public String getSuffix() {
		return length.format();
	}
}
