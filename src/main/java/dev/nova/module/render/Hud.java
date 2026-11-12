package dev.nova.module.render;

import dev.nova.gui.Theme;
import dev.nova.gui.render.Fonts;
import dev.nova.gui.render.Fonts.Weight;
import dev.nova.gui.render.Icons;
import dev.nova.gui.render.Shapes;
import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.module.Modules;
import dev.nova.setting.MultiSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Hud extends Module {
	private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private final MultiSetting elements = multi("Элементы")
		.add("Ватермарка", true).add("Модули", true).add("Координаты", true).add("FPS", true).add("Броня", false);

	private record Line(String name, String suffix, float width) {
	}

	public Hud() {
		super("Hud", "Игровой интерфейс клиента", Category.VISUALS);
		hidden = true;
		setEnabled(true);
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		if (mc.options.hudHidden || mc.getDebugHud().shouldShowDebugHud()) return;

		// The HUD is laid out for a 540px tall screen and scaled up from there.
		float pixels = Math.max(1f, mc.getWindow().getFramebufferHeight() / 540f);
		float scale = pixels / mc.getWindow().getScaleFactor();
		float width = context.getScaledWindowWidth() / scale;

		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.scale(scale, scale);
		drawInfo(context);
		if (elements.is("Модули")) drawModules(context, width);
		matrices.popMatrix();

		if (elements.is("Броня")) drawArmor(context);
	}

	private void drawInfo(DrawContext context) {
		float x = 8;
		if (elements.is("Ватермарка")) {
			float textWidth = Fonts.width("Nova", 11, Weight.SEMIBOLD) + 4 + Fonts.width("Client", 11, Weight.MEDIUM);
			float pillWidth = 5 + 16 + 7 + textWidth + 10;
			Shapes.rect(context, x, 8, pillWidth, 26, 8, Theme.surface);
			Shapes.gradientDown(context, x + 5, 13, 16, 16, 5, Theme.accent, Theme.accentDeep);
			Icons.logo(context, x + 8.5f, 16.5f, 9, Theme.TEXT);
			Fonts.draw(context, "Nova", x + 28, 14.4f, 11, Theme.TEXT, Weight.SEMIBOLD);
			Fonts.draw(context, "Client", x + 32 + Fonts.width("Nova", 11, Weight.SEMIBOLD), 14.4f, 11, Theme.textSoft, Weight.MEDIUM);
			x += pillWidth + 4;
		}
		if (elements.is("FPS")) x += pill(context, x, "FPS", Integer.toString(mc.getCurrentFps())) + 4;
		if (elements.is("Координаты")) {
			pill(context, x, "XYZ", String.format("%.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
		}
	}

	/** Small caption + value chip; returns its width. */
	private static float pill(DrawContext context, float x, String caption, String value) {
		float captionWidth = Fonts.width(caption, 9.5f, Weight.SEMIBOLD);
		float pillWidth = 9 + captionWidth + 5 + Fonts.width(value, 10.5f, Weight.SEMIBOLD) + 9;
		Shapes.rect(context, x, 8, pillWidth, 26, 8, Theme.surface);
		Fonts.draw(context, caption, x + 9, 15.4f, 9.5f, Theme.accent, Weight.SEMIBOLD);
		Fonts.draw(context, value, x + 9 + captionWidth + 5, 14.7f, 10.5f, Theme.TEXT, Weight.SEMIBOLD);
		return pillWidth;
	}

	private void drawModules(DrawContext context, float screenWidth) {
		List<Line> lines = new ArrayList<>();
		for (Module module : Modules.ALL) {
			if (!module.isEnabled() || module.isHidden()) continue;
			String suffix = module.getSuffix() == null ? "" : module.getSuffix();
			float width = Fonts.width(module.getName(), 10, Weight.SEMIBOLD);
			if (!suffix.isEmpty()) width += 4 + Fonts.width(suffix, 10, Weight.MEDIUM);
			lines.add(new Line(module.getName(), suffix, width));
		}
		lines.sort(Comparator.comparingDouble((Line line) -> line.width).reversed());

		float y = 8;
		for (int i = 0; i < lines.size(); i++) {
			Line line = lines.get(i);
			float rowWidth = line.width + 8 + 9;
			float x = screenWidth - 8 - rowWidth;
			// The accent travels down the list over time.
			double phase = System.currentTimeMillis() % 3000L / 3000.0 * Math.PI * 2 - i * 0.45;
			int accent = Theme.mix(Theme.accent, Theme.accentDeep, (float) (Math.sin(phase) * 0.5 + 0.5));
			Shapes.rect(context, x, y, rowWidth, 16, 5, Theme.surface);
			Shapes.rect(context, x + rowWidth - 4.5f, y + 4, 2, 8, 1, accent);
			Fonts.draw(context, line.name, x + 8, y + 2.2f, 10, Theme.TEXT, Weight.SEMIBOLD);
			if (!line.suffix.isEmpty()) {
				Fonts.draw(context, line.suffix, x + 8 + Fonts.width(line.name, 10, Weight.SEMIBOLD) + 4, y + 2.2f, 10, Theme.muted, Weight.MEDIUM);
			}
			y += 18;
		}
	}

	/** Worn armor with remaining durability, to the right of the hotbar. */
	private void drawArmor(DrawContext context) {
		int x = context.getScaledWindowWidth() / 2 + 98;
		int y = context.getScaledWindowHeight() - 20;
		for (EquipmentSlot slot : ARMOR) {
			ItemStack stack = mc.player.getEquippedStack(slot);
			if (stack.isEmpty()) continue;
			context.drawItem(stack, x, y);
			context.drawStackOverlay(mc.textRenderer, stack, x, y);
			x += 18;
		}
	}
}
