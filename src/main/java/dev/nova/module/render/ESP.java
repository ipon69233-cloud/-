package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ColorSetting;
import dev.nova.setting.ModeSetting;
import dev.nova.setting.MultiSetting;
import dev.nova.util.EntityUtil;
import dev.nova.util.Friends;
import dev.nova.util.WorldOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ESP extends Module {
	public static final int FRIEND_COLOR = 0xFF55FF55;

	private final ModeSetting mode = mode("Режим", "Каркас", "Каркас", "2D");
	private final ColorSetting color = color("Цвет", 0xFF3B9BFF);
	private final MultiSetting targets = multi("Цели").add("Игроки", true).add("Мобы", true).add("Животные", false).add("Предметы", false);

	private final float[] point = new float[2];

	public ESP() {
		super("ESP", "Подсветка игроков сквозь стены", Category.VISUALS);
	}

	private int colorOf(Entity entity) {
		return Friends.isFriend(entity) ? FRIEND_COLOR : color.get();
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		boolean flat = mode.is("2D");
		WorldOverlay.Lines lines = WorldOverlay.begin(context);
		for (Entity entity : mc.world.getEntities()) {
			if (!EntityUtil.matches(entity, targets)) continue;
			Box box = EntityUtil.lerpedBox(entity, tickDelta);
			if (flat) renderFlat(context, entity, box, colorOf(entity));
			else lines.box(box, colorOf(entity), 1.5f);
		}
		lines.submit(context);
	}

	/** Rectangle around the projected bounding box with a health bar on its left. */
	private void renderFlat(DrawContext context, Entity entity, Box box, int color) {
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		for (int i = 0; i < 8; i++) {
			Vec3d corner = new Vec3d((i & 1) == 0 ? box.minX : box.maxX, (i & 2) == 0 ? box.minY : box.maxY, (i & 4) == 0 ? box.minZ : box.maxZ);
			if (!WorldOverlay.project(corner, point)) return;
			minX = Math.min(minX, point[0]);
			minY = Math.min(minY, point[1]);
			maxX = Math.max(maxX, point[0]);
			maxY = Math.max(maxY, point[1]);
		}
		int x1 = Math.round(minX);
		int y1 = Math.round(minY);
		int x2 = Math.round(maxX);
		int y2 = Math.round(maxY);
		context.fill(x1, y1, x2, y1 + 1, color);
		context.fill(x1, y2 - 1, x2, y2, color);
		context.fill(x1, y1, x1 + 1, y2, color);
		context.fill(x2 - 1, y1, x2, y2, color);

		if (!(entity instanceof LivingEntity living)) return;
		float ratio = Math.max(0f, Math.min(1f, living.getHealth() / living.getMaxHealth()));
		int barRight = x1 - 2;
		int barLeft = barRight - 2;
		context.fill(barLeft - 1, y1 - 1, barRight + 1, y2 + 1, 0xAA000000);
		int filled = Math.round((y2 - y1) * ratio);
		int barColor = ratio > 0.5f ? 0xFF55FF55 : ratio > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;
		context.fill(barLeft, y2 - filled, barRight, y2, barColor);
	}

	@Override
	public String getSuffix() {
		return mode.get();
	}
}
