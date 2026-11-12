package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.util.Friends;
import dev.nova.util.WorldOverlay;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/** The vanilla label of the same players is hidden in EntityRendererMixin. */
public class NameTags extends Module {
	private final BoolSetting background = bool("Фон", true);
	private final NumberSetting size = number("Размер", 1, 0.5, 2, 0.05);

	private final float[] point = new float[2];

	public NameTags() {
		super("NameTags", "Улучшенные таблички над игроками", Category.VISUALS);
	}

	public boolean replaces(Entity entity) {
		return isEnabled() && entity instanceof PlayerEntity && entity != mc.player;
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		TextRenderer text = mc.textRenderer;
		for (PlayerEntity player : mc.world.getPlayers()) {
			if (!replaces(player) || !player.isAlive()) continue;
			Vec3d anchor = player.getLerpedPos(tickDelta).add(0, player.getHeight() + 0.35, 0);
			if (!WorldOverlay.project(anchor, point)) continue;

			float health = player.getHealth() + player.getAbsorptionAmount();
			String name = player.getGameProfile().name();
			String label = (Friends.isFriend(player) ? "§a" : "§f") + name + " " + healthColor(health) + String.format("%.1f", health);
			int width = text.getWidth(label);

			context.getMatrices().pushMatrix();
			context.getMatrices().translate(point[0], point[1]);
			context.getMatrices().scale(size.getFloat(), size.getFloat());
			if (background.get()) context.fill(-width / 2 - 2, -10, width / 2 + 2, 1, 0x90000000);
			context.drawText(text, label, -width / 2, -9, 0xFFFFFFFF, true);
			context.getMatrices().popMatrix();
		}
	}

	private static String healthColor(float health) {
		return health > 14 ? "§a" : health > 7 ? "§e" : "§c";
	}
}
