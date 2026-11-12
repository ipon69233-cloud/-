package dev.nova.module.render;

import dev.nova.gui.Theme;
import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.module.Modules;
import dev.nova.setting.BoolSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Hud extends Module {
	private final BoolSetting watermark = bool("Watermark", true);
	private final BoolSetting moduleList = bool("ModuleList", true);
	private final BoolSetting info = bool("Info", true);

	public Hud() {
		super("HUD", "Список включённых функций, координаты и FPS", Category.VISUALS);
		hidden = true;
		setEnabled(true);
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		if (mc.options.hudHidden || mc.getDebugHud().shouldShowDebugHud()) return;
		TextRenderer text = mc.textRenderer;
		int y = 4;

		if (watermark.get()) {
			context.drawText(text, "Nova", 4, y, Theme.gradient(0), true);
			context.drawText(text, "Client", 4 + text.getWidth("Nova "), y, Theme.TEXT, true);
			y += 11;
		}
		if (info.get()) {
			context.drawText(text, "FPS " + mc.getCurrentFps(), 4, y, Theme.TEXT_DIM, true);
			String position = String.format("XYZ %.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
			context.drawText(text, position, 4, y + 10, Theme.TEXT_DIM, true);
		}
		if (moduleList.get()) renderModuleList(context, text);
	}

	private void renderModuleList(DrawContext context, TextRenderer text) {
		List<String> lines = new ArrayList<>();
		for (Module module : Modules.ALL) {
			if (!module.isEnabled() || module.isHidden()) continue;
			String suffix = module.getSuffix();
			lines.add(suffix == null ? module.getName() : module.getName() + " §7" + suffix);
		}
		lines.sort(Comparator.comparingInt((String line) -> text.getWidth(line)).reversed());

		int right = context.getScaledWindowWidth();
		int y = 2;
		for (int i = 0; i < lines.size(); i++) {
			int width = text.getWidth(lines.get(i));
			int color = Theme.gradient(i);
			context.fill(right - width - 6, y, right - 2, y + 11, 0x90000000);
			context.fill(right - 2, y, right, y + 11, color);
			context.drawText(text, lines.get(i), right - width - 4, y + 2, color, true);
			y += 11;
		}
	}
}
