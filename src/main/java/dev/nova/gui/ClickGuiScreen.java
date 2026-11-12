package dev.nova.gui;

import dev.nova.config.Config;
import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.module.Modules;
import dev.nova.setting.BoolSetting;
import dev.nova.setting.ModeSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClickGuiScreen extends Screen {
	// Five panels fit side by side at the default GUI scale of a 1080p window.
	private static final int PANEL_WIDTH = 92;
	private static final int PANEL_GAP = 3;
	private static final int HEADER_HEIGHT = 16;
	private static final int ROW_HEIGHT = 14;

	private static final class Panel {
		int x;
		int y;
		boolean open = true;
	}

	private enum Kind {
		HEADER, MODULE, SETTING, BIND
	}

	private record Row(Kind kind, Panel panel, Module module, Setting<?> setting, int x, int y, int width, int height) {
		boolean contains(double mouseX, double mouseY) {
			return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
		}
	}

	// Static so the layout survives closing and reopening the menu.
	private static final Map<Category, Panel> PANELS = new EnumMap<>(Category.class);
	private static final Set<Module> EXPANDED = new HashSet<>();
	private static int arrangedWidth = -1;

	static {
		for (Category category : Category.values()) PANELS.put(category, new Panel());
	}

	/** Rows laid out by the last render call; clicks are resolved against them. */
	private final List<Row> rows = new ArrayList<>();
	private Panel dragging;
	private int dragOffsetX;
	private int dragOffsetY;
	private NumberSetting sliding;
	private int sliderX;
	private int sliderWidth;
	private Module binding;

	public ClickGuiScreen() {
		super(Text.literal("Nova Client"));
	}

	@Override
	protected void init() {
		// Lay the panels out again whenever the scaled window width changes.
		if (arrangedWidth == width) return;
		arrangedWidth = width;

		// Masonry layout: each panel goes below the shortest column, so one row is used when it fits.
		int count = Category.values().length;
		int columns = Math.clamp((width - 4 + PANEL_GAP) / (PANEL_WIDTH + PANEL_GAP), 1, count);
		int startX = (width - columns * PANEL_WIDTH - (columns - 1) * PANEL_GAP) / 2;
		int[] bottoms = new int[columns];
		Arrays.fill(bottoms, 22);
		for (Category category : Category.values()) {
			int column = 0;
			for (int i = 1; i < columns; i++) {
				if (bottoms[i] < bottoms[column]) column = i;
			}
			Panel panel = PANELS.get(category);
			panel.x = startX + column * (PANEL_WIDTH + PANEL_GAP);
			panel.y = bottoms[column];
			long modules = Modules.ALL.stream().filter(module -> module.getCategory() == category).count();
			bottoms[column] += HEADER_HEIGHT + ROW_HEIGHT * (int) modules + 6;
		}
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		// A light dim instead of the vanilla blur keeps the world readable while tweaking.
		context.fill(0, 0, width, height, 0x70000000);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		TextRenderer text = MinecraftClient.getInstance().textRenderer;
		rows.clear();
		Module hovered = null;

		for (Category category : Category.values()) {
			Panel panel = PANELS.get(category);
			int x = panel.x;
			int y = panel.y;

			context.fill(x, y, x + PANEL_WIDTH, y + HEADER_HEIGHT, Theme.ACCENT);
			context.drawCenteredTextWithShadow(text, category.title, x + PANEL_WIDTH / 2, y + 4, Theme.TEXT);
			rows.add(new Row(Kind.HEADER, panel, null, null, x, y, PANEL_WIDTH, HEADER_HEIGHT));
			y += HEADER_HEIGHT;
			if (!panel.open) continue;

			for (Module module : Modules.ALL) {
				if (module.getCategory() != category) continue;
				Row row = new Row(Kind.MODULE, panel, module, null, x, y, PANEL_WIDTH, ROW_HEIGHT);
				rows.add(row);
				boolean over = row.contains(mouseX, mouseY);
				if (over) hovered = module;

				context.fill(x, y, x + PANEL_WIDTH, y + ROW_HEIGHT, Theme.PANEL);
				if (module.isEnabled()) context.fill(x, y, x + 2, y + ROW_HEIGHT, Theme.ACCENT);
				if (over) context.fill(x, y, x + PANEL_WIDTH, y + ROW_HEIGHT, Theme.ROW_HOVER);
				context.drawText(text, module.getName(), x + 6, y + 3, module.isEnabled() ? Theme.ACCENT_ALT : Theme.TEXT, true);
				boolean expanded = EXPANDED.contains(module);
				context.drawText(text, expanded ? "-" : "+", x + PANEL_WIDTH - 10, y + 3, Theme.TEXT_DIM, false);
				y += ROW_HEIGHT;

				if (expanded) y = renderSettings(context, text, panel, module, x, y, mouseX, mouseY);
			}
		}

		renderFooter(context, text, hovered);
		super.render(context, mouseX, mouseY, deltaTicks);
	}

	private int renderSettings(DrawContext context, TextRenderer text, Panel panel, Module module, int x, int y, int mouseX, int mouseY) {
		for (Setting<?> setting : module.getSettings()) {
			Row row = new Row(Kind.SETTING, panel, module, setting, x, y, PANEL_WIDTH, ROW_HEIGHT);
			rows.add(row);
			context.fill(x, y, x + PANEL_WIDTH, y + ROW_HEIGHT, Theme.SETTINGS_BG);

			String value;
			if (setting instanceof NumberSetting number) {
				int filled = (int) Math.round((PANEL_WIDTH - 4) * number.getProgress());
				context.fill(x + 2, y + 1, x + 2 + filled, y + ROW_HEIGHT - 1, 0x808B5CF6);
				value = number.format();
			} else if (setting instanceof BoolSetting bool) {
				value = bool.get() ? "§aON" : "§cOFF";
			} else {
				value = String.valueOf(setting.get());
			}
			if (row.contains(mouseX, mouseY)) context.fill(x, y, x + PANEL_WIDTH, y + ROW_HEIGHT, Theme.ROW_HOVER);
			context.drawText(text, setting.getName(), x + 6, y + 3, Theme.TEXT_DIM, false);
			context.drawText(text, value, x + PANEL_WIDTH - 5 - text.getWidth(value), y + 3, Theme.TEXT, false);
			y += ROW_HEIGHT;
		}

		Row bind = new Row(Kind.BIND, panel, module, null, x, y, PANEL_WIDTH, ROW_HEIGHT);
		rows.add(bind);
		context.fill(x, y, x + PANEL_WIDTH, y + ROW_HEIGHT, Theme.SETTINGS_BG);
		if (bind.contains(mouseX, mouseY)) context.fill(x, y, x + PANEL_WIDTH, y + ROW_HEIGHT, Theme.ROW_HOVER);
		String key = binding == module ? "..." : keyName(module.getKey());
		context.drawText(text, "Bind", x + 6, y + 3, Theme.TEXT_DIM, false);
		context.drawText(text, key, x + PANEL_WIDTH - 5 - text.getWidth(key), y + 3, Theme.ACCENT_ALT, false);
		return y + ROW_HEIGHT;
	}

	private void renderFooter(DrawContext context, TextRenderer text, Module hovered) {
		String hint = binding != null
			? "Нажми клавишу для " + binding.getName() + " (Esc / Delete — убрать)"
			: hovered != null ? hovered.getDescription() : "ЛКМ — вкл/выкл, ПКМ — настройки, колесо — прокрутка";
		int hintWidth = text.getWidth(hint);
		int x = (width - hintWidth) / 2;
		int y = height - 18;
		context.fill(x - 5, y - 4, x + hintWidth + 5, y + 12, Theme.PANEL);
		context.drawText(text, hint, x, y, Theme.TEXT, false);
	}

	private static String keyName(int key) {
		return key < 0 ? "нет" : InputUtil.Type.KEYSYM.createFromCode(key).getLocalizedText().getString();
	}

	private Row rowAt(double mouseX, double mouseY) {
		// Later rows are drawn on top, so they win when panels overlap.
		for (int i = rows.size() - 1; i >= 0; i--) {
			if (rows.get(i).contains(mouseX, mouseY)) return rows.get(i);
		}
		return null;
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		Row row = rowAt(click.x(), click.y());
		if (row == null) return super.mouseClicked(click, doubled);
		boolean left = click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT;
		boolean right = click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
		binding = null;

		switch (row.kind) {
			case HEADER -> {
				if (left) {
					dragging = row.panel;
					dragOffsetX = (int) click.x() - row.panel.x;
					dragOffsetY = (int) click.y() - row.panel.y;
				} else if (right) {
					row.panel.open = !row.panel.open;
				}
			}
			case MODULE -> {
				// Toggling the menu module from inside the menu would only reopen it.
				if (left && row.module != Modules.CLICK_GUI) row.module.toggle();
				else if (right && !EXPANDED.remove(row.module)) EXPANDED.add(row.module);
			}
			case SETTING -> {
				if (row.setting instanceof BoolSetting bool) {
					bool.toggle();
				} else if (row.setting instanceof ModeSetting mode) {
					mode.cycle(left);
				} else if (row.setting instanceof NumberSetting number && left) {
					sliding = number;
					sliderX = row.x + 2;
					sliderWidth = row.width - 4;
					number.setProgress((click.x() - sliderX) / sliderWidth);
				}
			}
			case BIND -> {
				if (left) binding = row.module;
				else if (right) row.module.setKey(-1);
			}
		}
		return true;
	}

	@Override
	public boolean mouseDragged(Click click, double offsetX, double offsetY) {
		if (dragging != null) {
			dragging.x = (int) click.x() - dragOffsetX;
			dragging.y = (int) click.y() - dragOffsetY;
			return true;
		}
		if (sliding != null) {
			sliding.setProgress((click.x() - sliderX) / sliderWidth);
			return true;
		}
		return super.mouseDragged(click, offsetX, offsetY);
	}

	@Override
	public boolean mouseReleased(Click click) {
		dragging = null;
		sliding = null;
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		for (Panel panel : PANELS.values()) panel.y += (int) (verticalAmount * 14);
		return true;
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (binding == null) return super.keyPressed(input);
		int key = input.key();
		boolean clear = key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE;
		binding.setKey(clear ? -1 : key);
		binding = null;
		return true;
	}

	@Override
	public void removed() {
		Config.save();
		super.removed();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
