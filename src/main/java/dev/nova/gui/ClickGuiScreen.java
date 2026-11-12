package dev.nova.gui;

import dev.nova.config.Config;
import dev.nova.gui.render.Fonts;
import dev.nova.gui.render.Fonts.Weight;
import dev.nova.gui.render.Icons;
import dev.nova.gui.render.Shapes;
import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.module.Modules;
import dev.nova.setting.BoolSetting;
import dev.nova.setting.ColorSetting;
import dev.nova.setting.ModeSetting;
import dev.nova.setting.MultiSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.setting.Setting;
import dev.nova.util.Friends;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;

/**
 * The client menu: a single window with a category sidebar, a header, a search field and
 * module cards in two columns. Laid out on a fixed 832x512 canvas that is scaled to the window.
 */
public class ClickGuiScreen extends Screen {
	private static final float WIDTH = 832, HEIGHT = 512;
	/** Scrollable viewport of the module cards. */
	private static final float VIEW_X = 104, VIEW_Y = 78, VIEW_W = 694, VIEW_H = 410;
	private static final float CARD_W = 343, CARD_GAP = 8, CARD_HEAD = 78;
	private static final float SETTINGS_W = CARD_W - 32;
	private static final float THEMES_X = 92, THEMES_Y = 56, THEMES_W = 310, THEMES_H = 280;

	private enum View {
		MODULES, PROFILE
	}

	/** A clickable region registered while drawing; the action receives the mouse button. */
	private record Hit(float x, float y, float w, float h, boolean inViewport, IntConsumer action) {
	}

	private record AnimationKey(Object owner, String name) {
	}

	private static final class Animation {
		float value;
		long frame = -1;

		Animation(float value) {
			this.value = value;
		}
	}

	// Static so the menu reopens where it was left.
	private static Category category = Category.COMBAT;
	private static View view = View.MODULES;
	private static final Set<Object> OPEN = new HashSet<>();
	private static final Map<AnimationKey, Animation> ANIMATIONS = new HashMap<>();
	private static long frame;

	private final List<Hit> hits = new ArrayList<>();
	private float scale = 1f, originX, originY;
	private float mouseX, mouseY;
	private float frameSeconds;
	private long lastFrame;

	private String search = "";
	private boolean searchFocused;
	private Module binding;
	private NumberSetting sliding;
	private float sliderX, sliderW;
	private boolean themesOpen;
	private boolean skipChar;
	private float scroll, scrollTarget, maxScroll;
	private float themesScroll;
	/** The key press that opened the menu is also delivered to it and must not close it again. */
	private boolean openingKeyPending = true;

	public ClickGuiScreen() {
		super(Text.literal("Nova Client"));
	}

	/** True while keystrokes belong to the menu, so movement helpers must not react to them. */
	public boolean isTyping() {
		return searchFocused || binding != null;
	}

	// ---------------------------------------------------------------- frame setup

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		applyBlur(context);
		context.fill(0, 0, width, height, 0x8C000000);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		MinecraftClient mc = MinecraftClient.getInstance();
		float pixels = Math.min(mc.getWindow().getFramebufferWidth() / WIDTH, mc.getWindow().getFramebufferHeight() / HEIGHT) * 0.86f;
		scale = pixels / mc.getWindow().getScaleFactor();
		originX = (width - WIDTH * scale) / 2f;
		originY = (height - HEIGHT * scale) / 2f;
		this.mouseX = (mouseX - originX) / scale;
		this.mouseY = (mouseY - originY) / scale;

		long now = System.nanoTime();
		frameSeconds = lastFrame == 0 ? 0f : Math.min(0.1f, (now - lastFrame) / 1.0e9f);
		lastFrame = now;
		frame++;
		scroll += (scrollTarget - scroll) * ease(18f);
		hits.clear();
		Shapes.resetClips();

		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.translate(originX, originY);
		matrices.scale(scale, scale);
		drawSidebar(context);
		drawHeader(context);
		drawSearch(context);
		drawContent(context);
		if (themesOpen) drawThemes(context);
		matrices.popMatrix();
	}

	/** Fraction of the remaining distance an animation covers this frame. */
	private float ease(float speed) {
		return 1f - (float) Math.exp(-frameSeconds * speed);
	}

	private float animate(Object owner, String name, boolean on) {
		Animation animation = ANIMATIONS.computeIfAbsent(new AnimationKey(owner, name), key -> new Animation(on ? 1f : 0f));
		// Advance at most once per frame, however often the value is read during layout.
		if (animation.frame != frame) {
			animation.frame = frame;
			float target = on ? 1f : 0f;
			animation.value += (target - animation.value) * ease(16f);
			if (Math.abs(target - animation.value) < 0.002f) animation.value = target;
		}
		return animation.value;
	}

	private boolean hovered(float x, float y, float w, float h) {
		return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
	}

	private boolean hoveredInViewport(float x, float y, float w, float h) {
		return hovered(x, y, w, h) && hovered(VIEW_X, VIEW_Y, VIEW_W, VIEW_H) && !overThemes();
	}

	private boolean overThemes() {
		return themesOpen && hovered(THEMES_X, THEMES_Y, THEMES_W, THEMES_H);
	}

	private void hit(float x, float y, float w, float h, IntConsumer action) {
		hits.add(new Hit(x, y, w, h, false, action));
	}

	private void viewportHit(float x, float y, float w, float h, IntConsumer action) {
		hits.add(new Hit(x, y, w, h, true, action));
	}

	private static void label(DrawContext context, String text, float x, float y, float size, int color, Weight weight) {
		Fonts.draw(context, text, x, y, size, color, weight);
	}

	/** Y of a line box that centers text of the given size in a box. */
	private static float centered(float y, float height, float size) {
		return y + (height - size * 1.2f) / 2f;
	}

	// ---------------------------------------------------------------- sidebar

	private void drawSidebar(DrawContext context) {
		Shapes.rect(context, 8, 8, 72, 496, 16, 4, 4, 16, Theme.surface, Theme.surface, false);

		Shapes.gradientDown(context, 24, 24, 40, 40, 11, Theme.accent, Theme.accentDeep);
		Icons.logo(context, 34, 34, 20, Theme.TEXT);
		hit(24, 24, 40, 40, button -> themesOpen = !themesOpen);
		Shapes.rect(context, 28, 72, 32, 1, 0, Theme.w08);

		Category[] categories = Category.values();
		for (int i = 0; i < categories.length; i++) {
			Category entry = categories[i];
			float y = 105 + i * 56;
			boolean active = view == View.MODULES && category == entry && search.isEmpty();
			float glow = animate(entry, "nav", active);
			boolean hover = hovered(24, y, 40, 40);
			Shapes.rect(context, 24, y, 40, 40, 8, Theme.mix(hover ? Theme.w03 : Theme.w01, Theme.alpha(Theme.accent, 0.14f), glow));
			Shapes.outline(context, 24, y, 40, 40, 8, 1, Theme.mix(Theme.w03, Theme.alpha(Theme.accent, 0.55f), glow));
			Icons.category(context, entry, 34.5f, y + 10.5f, 19, Theme.mix(hover ? Theme.textSoft : Theme.w24, Theme.accent, glow));
			hit(24, y, 40, 40, button -> {
				category = entry;
				view = View.MODULES;
				search = "";
				scroll = scrollTarget = 0;
			});
		}

		boolean profile = view == View.PROFILE;
		Shapes.gradientDown(context, 24, 448, 40, 40, 20, Theme.mix(Theme.w24, Theme.accent, profile ? 1f : 0.6f), Theme.mix(Theme.w24, Theme.accentDeep, profile ? 1f : 0.6f));
		Shapes.circle(context, 44, 468, 18, Theme.background | 0xFF000000);
		Icons.user(context, 34.5f, 458.5f, 19, Theme.textSoft);
		hit(24, 448, 40, 40, button -> view = profile ? View.MODULES : View.PROFILE);
	}

	// ---------------------------------------------------------------- header and search

	private void drawHeader(DrawContext context) {
		Shapes.rect(context, 88, 8, 472, 46, 4, Theme.surface);
		float x = 104;
		Icons.logo(context, x, 24.5f, 13, Theme.muted);
		x += 13 + 6;
		label(context, "nova client", x, centered(8, 46, 12), 12, Theme.muted, Weight.MEDIUM);
		x += Fonts.width("nova client", 12, Weight.MEDIUM) + 6;
		Icons.chevronRight(context, x, 26.5f, 9, Theme.w24);
		x += 9 + 6;
		boolean searching = !search.isEmpty();
		String title = view == View.PROFILE ? "Профиль" : searching ? "Поиск" : category.title;
		if (view == View.PROFILE) Icons.user(context, x, 24.5f, 13, Theme.accent);
		else if (searching) Icons.search(context, x, 24.5f, 13, Theme.accent);
		else Icons.category(context, category, x, 24.5f, 13, Theme.accent);
		x += 13 + 6;
		label(context, title, x, centered(8, 46, 12), 12, Theme.TEXT, Weight.SEMIBOLD);

		// The menu key on the right; clicking it rebinds the menu.
		float right = 560 - 16;
		Icons.keyboard(context, right - 14, 24, 14, Theme.accent);
		boolean waitingMenuKey = binding == Modules.CLICK_GUI;
		String key = waitingMenuKey ? "..." : keyName(Modules.CLICK_GUI.getKey());
		float keyWidth = Fonts.width(key, 12, Weight.SEMIBOLD);
		label(context, key, right - 14 - 7 - keyWidth, centered(8, 46, 12), 12, Theme.TEXT, Weight.SEMIBOLD);
		hit(right - 14 - 7 - keyWidth - 4, 14, keyWidth + 29, 34, button -> binding = waitingMenuKey ? null : Modules.CLICK_GUI);

		long active = Modules.ALL.stream().filter(module -> module.isEnabled() && module != Modules.CLICK_GUI).count();
		String status = active + " активно";
		float chipWidth = Fonts.width(status, 9.5f, Weight.SEMIBOLD) + 26 + 9;
		float chipX = right - 14 - 7 - keyWidth - 14 - chipWidth;
		Shapes.rect(context, chipX, 14, chipWidth, 34, 6, Theme.w02);
		Shapes.outline(context, chipX, 14, chipWidth, 34, 6, 1, Theme.w06);
		Shapes.circle(context, chipX + 15, 31, 2.5f, Theme.accent);
		label(context, status, chipX + 22, centered(14, 34, 9.5f), 9.5f, Theme.textSoft, Weight.SEMIBOLD);
	}

	private void drawSearch(DrawContext context) {
		float focus = animate("search", "focus", searchFocused);
		Shapes.rect(context, 568, 8, 256, 46, 4, 16, 4, 4, Theme.mix(Theme.surface, Theme.mix(Theme.surface, Theme.w04, 0.6f), focus), Theme.mix(Theme.surface, Theme.mix(Theme.surface, Theme.w04, 0.6f), focus), false);
		Shapes.outline(context, 568, 8, 256, 46, 4, 16, 4, 4, 1, Theme.alpha(Theme.accent, 0.4f * focus));

		float y = centered(8, 46, 12);
		String shown = search.isEmpty() && !searchFocused ? "Поиск..." : Fonts.trim(search, 200, 12, Weight.MEDIUM);
		label(context, shown, 584, y, 12, search.isEmpty() ? Theme.muted : Theme.TEXT, Weight.MEDIUM);
		if (searchFocused && System.currentTimeMillis() % 1000 < 500) {
			Shapes.rect(context, 584 + Fonts.width(search.isEmpty() ? "" : shown, 12, Weight.MEDIUM) + 1, 24.5f, 1, 13, 0, Theme.accent);
		}
		boolean lit = searchFocused || !search.isEmpty();
		Icons.search(context, 824 - 16 - 13, 24.5f, 13, lit ? Theme.accent : Theme.w16);
		hit(568, 8, 256, 46, button -> {
			searchFocused = true;
			binding = null;
		});
		// The magnifier doubles as a clear button.
		hit(824 - 16 - 19, 18, 25, 26, button -> {
			search = "";
			searchFocused = false;
		});
	}

	// ---------------------------------------------------------------- content

	private List<Module> visibleModules() {
		String query = search.trim().toLowerCase(Locale.ROOT);
		List<Module> modules = new ArrayList<>();
		for (Module module : Modules.ALL) {
			if (module == Modules.CLICK_GUI) continue;
			boolean matches = query.isEmpty()
				? module.getCategory() == category
				: module.getName().toLowerCase(Locale.ROOT).contains(query) || module.getDescription().toLowerCase(Locale.ROOT).contains(query);
			if (matches) modules.add(module);
		}
		return modules;
	}

	private void drawContent(DrawContext context) {
		Shapes.rect(context, 88, 62, 736, 442, 4, 4, 16, 4, Theme.surface, Theme.surface, false);
		if (view == View.PROFILE) {
			drawProfile(context);
			return;
		}

		List<Module> modules = visibleModules();
		Shapes.pushClip(context, (int) VIEW_X, (int) VIEW_Y, (int) (VIEW_X + VIEW_W), (int) (VIEW_Y + VIEW_H));
		float[] bottoms = {0, 0};
		for (int i = 0; i < modules.size(); i++) {
			int column = i % 2;
			float x = VIEW_X + column * (CARD_W + CARD_GAP);
			float y = VIEW_Y + bottoms[column] - Math.round(scroll);
			float height = cardHeight(modules.get(i));
			// Cards outside the viewport only take part in the layout.
			if (y + height > VIEW_Y && y < VIEW_Y + VIEW_H) drawCard(context, modules.get(i), x, y, height);
			bottoms[column] += height + CARD_GAP;
		}
		Shapes.popClip(context);

		if (modules.isEmpty()) {
			Fonts.drawCentered(context, "Ничего не найдено", 88 + 368, 270, 12, Theme.muted, Weight.MEDIUM);
		}

		float contentHeight = Math.max(bottoms[0], bottoms[1]) - CARD_GAP;
		maxScroll = Math.max(0, contentHeight - VIEW_H);
		scrollTarget = Math.clamp(scrollTarget, 0, maxScroll);
		if (maxScroll > 0) {
			float thumb = Math.max(24, VIEW_H * VIEW_H / contentHeight);
			Shapes.rect(context, 815, VIEW_Y, 4, VIEW_H, 2, Theme.w03);
			Shapes.rect(context, 815, VIEW_Y + (VIEW_H - thumb) * Math.clamp(scroll / maxScroll, 0f, 1f), 4, thumb, 2, Theme.w16);
		}
	}

	private float cardHeight(Module module) {
		if (module.getSettings().isEmpty()) return CARD_HEAD;
		return CARD_HEAD + settingsHeight(module) * animate(module, "open", OPEN.contains(module));
	}

	private void drawCard(DrawContext context, Module module, float x, float y, float height) {
		boolean enabled = module.isEnabled();
		float on = animate(module, "on", enabled);
		boolean hasSettings = !module.getSettings().isEmpty();
		boolean open = OPEN.contains(module);
		boolean hover = hoveredInViewport(x, y, CARD_W, CARD_HEAD);

		Shapes.rect(context, x, y, CARD_W, height, 8, Theme.mix(hover ? Theme.w02 : Theme.w01, hover ? Theme.w03 : Theme.w02, on));
		Shapes.outline(context, x, y, CARD_W, height, 8, 1, Theme.mix(Theme.w02, Theme.w06, on));
		Shapes.gradient(context, x + 8, y, CARD_W - 16, 1, 0, Theme.fade(Theme.accent, on), Theme.fade(Theme.accentDeep, on));
		viewportHit(x, y, CARD_W, CARD_HEAD, button -> {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) module.toggle();
			else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && hasSettings) toggleOpen(module);
			else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) binding = module;
		});

		Shapes.gradientDown(context, x + 16, y + 18, 2, 10, 1, Theme.fade(Theme.accent, on), Theme.fade(Theme.accentDeep, on));
		label(context, module.getName(), x + 16 + 11 * on, y + 15, 12, Theme.mix(Theme.textSoft, Theme.TEXT, on), Weight.SEMIBOLD);

		// Switch.
		float switchX = x + CARD_W - 16 - 24;
		Shapes.rect(context, switchX, y + 16, 24, 14, 7, Theme.w12);
		Shapes.gradient(context, switchX, y + 16, 24, 14, 7, Theme.fade(Theme.accentDeep, on), Theme.fade(Theme.accent, on));
		Shapes.circle(context, switchX + 7 + 10 * on, y + 23, 5, Theme.mix(Theme.textSoft | 0xFF000000, Theme.TEXT, on));

		if (hasSettings) {
			float gearX = x + CARD_W - 48 - 16;
			boolean gearHover = hoveredInViewport(gearX - 2, y + 13, 20, 20);
			Icons.gear(context, gearX + 1.5f, y + 16.5f, 13, open ? Theme.accent : gearHover ? Theme.textSoft : Theme.w24);
			viewportHit(gearX - 2, y + 13, 20, 20, button -> toggleOpen(module));
		}

		List<String> lines = Fonts.wrap(module.getDescription(), CARD_W - 16 - 92, 10, Weight.MEDIUM, 2);
		for (int i = 0; i < lines.size(); i++) {
			label(context, lines.get(i), x + 16, y + 38 + i * 15, 10, Theme.muted, Weight.MEDIUM);
		}

		// Key bind chip.
		boolean waiting = binding == module;
		String key = waiting ? "..." : keyName(module.getKey());
		float keyWidth = Fonts.width(key, 10, Weight.SEMIBOLD);
		float chipWidth = keyWidth + 14 + 4 + 12;
		float chipX = x + CARD_W - 14 - chipWidth;
		boolean chipHover = hoveredInViewport(chipX, y + 34, chipWidth, 19);
		if (waiting) {
			float pulse = 0.6f + 0.4f * (float) Math.sin(System.currentTimeMillis() / 160.0);
			Shapes.rect(context, chipX, y + 34, chipWidth, 19, 7, Theme.alpha(Theme.accent, 0.2f * pulse));
			Shapes.outline(context, chipX, y + 34, chipWidth, 19, 7, 1, Theme.alpha(Theme.accent, 0.55f));
		} else if (chipHover) {
			Shapes.rect(context, chipX, y + 34, chipWidth, 19, 7, Theme.alpha(Theme.accent, 0.12f));
		}
		int keyColor = waiting ? Theme.accent : module.getKey() >= 0 ? Theme.textSoft : Theme.muted;
		label(context, key, chipX + 7, centered(y + 34, 19, 10), 10, keyColor, Weight.SEMIBOLD);
		Icons.keyboard(context, chipX + 7 + keyWidth + 4, y + 37.5f, 12, keyColor);
		viewportHit(chipX, y + 34, chipWidth, 19, button -> binding = waiting ? null : module);

		if (hasSettings && height > CARD_HEAD + 0.5f) {
			Shapes.pushClip(context, (int) x, (int) Math.floor(y + CARD_HEAD), (int) (x + CARD_W), (int) Math.ceil(y + height - 1));
			drawSettings(context, module, x + 16, y + CARD_HEAD, height >= CARD_HEAD + settingsHeight(module) - 1f);
			Shapes.popClip(context);
		}
	}

	private void toggleOpen(Object key) {
		if (!OPEN.remove(key)) OPEN.add(key);
	}

	// ---------------------------------------------------------------- settings

	private float settingsHeight(Module module) {
		float height = 1 + 11 + 14;
		for (Setting<?> setting : module.getSettings()) height += settingHeight(setting) + 11;
		return height - 11;
	}

	private float settingHeight(Setting<?> setting) {
		if (setting instanceof ModeSetting mode) {
			return 16 + animate(setting, "open", OPEN.contains(setting)) * (6 + 8 + mode.getModes().size() * 20 - 2);
		}
		if (setting instanceof MultiSetting multi) {
			int rows = chipRows(multi);
			return 12 + 6 + rows * 15 + (rows - 1) * 4;
		}
		if (setting instanceof ColorSetting) {
			int perRow = (int) ((SETTINGS_W + 6) / 26);
			int rows = (ColorSetting.PALETTE.length + perRow - 1) / perRow;
			return 15 + animate(setting, "open", OPEN.contains(setting)) * (9 + rows * 26 - 6 + 2);
		}
		return 12;
	}

	private static float chipWidth(String item) {
		return Fonts.width(item, 9, Weight.SEMIBOLD) + 16;
	}

	private static int chipRows(MultiSetting multi) {
		int rows = 1;
		float x = 0;
		for (String item : multi.items()) {
			float width = chipWidth(item);
			if (x > 0 && x + width > SETTINGS_W) {
				rows++;
				x = 0;
			}
			x += width + 4;
		}
		return rows;
	}

	private void drawSettings(DrawContext context, Module module, float x, float top, boolean interactive) {
		Shapes.rect(context, x - 15, top, CARD_W - 2, 1, 0, Theme.w06);
		float y = top + 1 + 11;
		for (Setting<?> setting : module.getSettings()) {
			float height = settingHeight(setting);
			if (setting instanceof BoolSetting bool) drawBool(context, bool, x, y, interactive);
			else if (setting instanceof NumberSetting number) drawNumber(context, number, x, y, interactive);
			else if (setting instanceof ModeSetting mode) drawMode(context, mode, x, y, height, interactive);
			else if (setting instanceof MultiSetting multi) drawMulti(context, multi, x, y, interactive);
			else if (setting instanceof ColorSetting color) drawColor(context, color, x, y, height, interactive);
			y += height + 11;
		}
	}

	private void drawBool(DrawContext context, BoolSetting setting, float x, float y, boolean interactive) {
		float on = animate(setting, "on", setting.get());
		label(context, setting.getName(), x, y, 10, Theme.textSoft, Weight.MEDIUM);
		float switchX = x + SETTINGS_W - 18;
		Shapes.rect(context, switchX, y + 1, 18, 10, 5, Theme.w12);
		Shapes.gradient(context, switchX, y + 1, 18, 10, 5, Theme.fade(Theme.accentDeep, on), Theme.fade(Theme.accent, on));
		Shapes.circle(context, switchX + 5 + 7 * on, y + 6, 3, Theme.TEXT);
		if (interactive) viewportHit(x, y - 3, SETTINGS_W, 18, button -> setting.toggle());
	}

	private void drawNumber(DrawContext context, NumberSetting setting, float x, float y, boolean interactive) {
		label(context, Fonts.trim(setting.getName(), 96, 10, Weight.MEDIUM), x, y, 10, Theme.textSoft, Weight.MEDIUM);
		Fonts.drawRight(context, setting.format(), x + SETTINGS_W, y, 10, Theme.TEXT, Weight.SEMIBOLD);
		float trackX = x + 100, trackW = SETTINGS_W - 100 - 40;
		float filled = trackW * (float) setting.getProgress();
		Shapes.rect(context, trackX, y + 4, trackW, 4, 2, Theme.w12);
		Shapes.gradient(context, trackX, y + 4, filled, 4, 2, Theme.accentDeep, Theme.accent);
		Shapes.rect(context, trackX + filled - 3.5f, y + 2.5f, 7, 7, 2, Theme.TEXT);
		if (interactive) {
			viewportHit(trackX - 4, y - 3, trackW + 8, 18, button -> {
				if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
				sliding = setting;
				sliderX = trackX;
				sliderW = trackW;
				setting.setProgress((mouseX - trackX) / trackW);
			});
		}
	}

	private void drawMode(DrawContext context, ModeSetting setting, float x, float y, float height, boolean interactive) {
		boolean open = OPEN.contains(setting);
		label(context, setting.getName(), x, centered(y, 16, 10), 10, Theme.textSoft, Weight.MEDIUM);
		float textWidth = Fonts.width(setting.get(), 9.5f, Weight.SEMIBOLD);
		float chipW = textWidth + 16 + 5 + 8;
		float chipX = x + SETTINGS_W - chipW;
		Shapes.rect(context, chipX, y, chipW, 16, 5, Theme.w03);
		Shapes.outline(context, chipX, y, chipW, 16, 5, 1, Theme.w06);
		label(context, setting.get(), chipX + 8, centered(y, 16, 9.5f), 9.5f, Theme.TEXT, Weight.SEMIBOLD);
		if (open) Icons.chevronUp(context, chipX + 8 + textWidth + 5, y + 4, 8, Theme.accent);
		else Icons.chevronDown(context, chipX + 8 + textWidth + 5, y + 4, 8, Theme.accent);
		if (interactive) viewportHit(x, y - 2, SETTINGS_W, 20, button -> toggleOpen(setting));

		if (height <= 16.5f) return;
		List<String> modes = setting.getModes();
		float listY = y + 16 + 6;
		float listH = 8 + modes.size() * 20 - 2;
		Shapes.pushClip(context, (int) x, (int) Math.floor(listY), (int) Math.ceil(x + SETTINGS_W), (int) Math.ceil(y + height));
		Shapes.rect(context, x, listY, SETTINGS_W, listH, 7, Theme.w02);
		Shapes.outline(context, x, listY, SETTINGS_W, listH, 7, 1, Theme.w06);
		boolean ready = interactive && height >= 16 + 6 + listH - 1f;
		for (int i = 0; i < modes.size(); i++) {
			String mode = modes.get(i);
			float rowY = listY + 4 + i * 20;
			boolean selected = setting.is(mode);
			boolean hover = ready && hoveredInViewport(x + 4, rowY, SETTINGS_W - 8, 18);
			if (selected || hover) Shapes.rect(context, x + 4, rowY, SETTINGS_W - 8, 18, 5, selected ? Theme.accentSoft : Theme.w03);
			label(context, mode, x + 12, centered(rowY, 18, 9.5f), 9.5f, selected ? Theme.TEXT : Theme.textSoft, Weight.MEDIUM);
			if (ready) {
				viewportHit(x + 4, rowY, SETTINGS_W - 8, 18, button -> {
					setting.set(mode);
					OPEN.remove(setting);
				});
			}
		}
		Shapes.popClip(context);
	}

	private void drawMulti(DrawContext context, MultiSetting setting, float x, float y, boolean interactive) {
		label(context, setting.getName(), x, y, 10, Theme.textSoft, Weight.MEDIUM);
		float chipX = 0;
		float chipY = y + 12 + 6;
		for (String item : setting.items()) {
			float width = chipWidth(item);
			if (chipX > 0 && chipX + width > SETTINGS_W) {
				chipX = 0;
				chipY += 19;
			}
			float on = animate(new AnimationKey(setting, item), "on", setting.is(item));
			Shapes.rect(context, x + chipX, chipY, width, 15, 4, Theme.mix(Theme.w02, Theme.accentSoft, on));
			Shapes.outline(context, x + chipX, chipY, width, 15, 4, 1, Theme.mix(Theme.w06, Theme.alpha(Theme.accent, 0.4f), on));
			label(context, item, x + chipX + 8, centered(chipY, 15, 9), 9, Theme.mix(Theme.textSoft, Theme.TEXT, on), Weight.SEMIBOLD);
			if (interactive) viewportHit(x + chipX, chipY - 1, width, 17, button -> setting.toggle(item));
			chipX += width + 4;
		}
	}

	private void drawColor(DrawContext context, ColorSetting setting, float x, float y, float height, boolean interactive) {
		label(context, setting.getName(), x, centered(y, 15, 10), 10, Theme.textSoft, Weight.MEDIUM);
		float swatchX = x + SETTINGS_W - 26;
		Shapes.rect(context, swatchX, y, 26, 15, 5, setting.get());
		Shapes.outline(context, swatchX, y, 26, 15, 5, 1, Theme.w12);
		Fonts.drawRight(context, setting.hex(), swatchX - 7, centered(y, 15, 9), 9, Theme.muted, Weight.SEMIBOLD);
		if (interactive) viewportHit(x, y - 2, SETTINGS_W, 19, button -> toggleOpen(setting));

		if (height <= 15.5f) return;
		Shapes.pushClip(context, (int) x - 3, (int) Math.floor(y + 15), (int) Math.ceil(x + SETTINGS_W) + 3, (int) Math.ceil(y + height));
		int perRow = (int) ((SETTINGS_W + 6) / 26);
		boolean ready = interactive && OPEN.contains(setting) && animate(setting, "open", true) > 0.95f;
		for (int i = 0; i < ColorSetting.PALETTE.length; i++) {
			int color = ColorSetting.PALETTE[i];
			float cellX = x + i % perRow * 26;
			float cellY = y + 15 + 9 + i / perRow * 26;
			boolean selected = setting.get() == color;
			Shapes.rect(context, cellX, cellY, 20, 20, 6, color);
			Shapes.outline(context, cellX, cellY, 20, 20, 6, selected ? 2 : 1, selected ? Theme.TEXT : 0x30FFFFFF);
			if (ready) {
				viewportHit(cellX, cellY, 20, 20, button -> {
					setting.set(color);
					OPEN.remove(setting);
				});
			}
		}
		Shapes.popClip(context);
	}

	// ---------------------------------------------------------------- profile

	private void drawProfile(DrawContext context) {
		MinecraftClient mc = MinecraftClient.getInstance();
		float x = VIEW_X, y = VIEW_Y, w = 704;
		Shapes.rect(context, x, y, w, 104, 14, Theme.w02);
		Shapes.outline(context, x, y, w, 104, 14, 1, Theme.w06);
		Shapes.gradientDown(context, x + 20, y + 20, 64, 64, 32, Theme.accent, Theme.accentDeep);
		Shapes.circle(context, x + 52, y + 52, 30, Theme.background | 0xFF000000);
		Icons.user(context, x + 37, y + 37, 30, Theme.textSoft);

		String name = mc.getSession().getUsername();
		label(context, name, x + 100, y + 22, 18, Theme.TEXT, Weight.SEMIBOLD);
		label(context, "Nova Client · Minecraft 1.21.11", x + 100, y + 46, 12, Theme.muted, Weight.MEDIUM);
		String world = mc.isInSingleplayer() ? "Одиночный мир" : "Сетевая игра";
		float chipW = Fonts.width(world, 10.5f, Weight.SEMIBOLD) + 20 + 12;
		Shapes.rect(context, x + 100, y + 66, chipW, 21, 10.5f, Theme.accentSoft);
		Shapes.outline(context, x + 100, y + 66, chipW, 21, 10.5f, 1, Theme.alpha(Theme.accent, 0.34f));
		Shapes.circle(context, x + 113, y + 76.5f, 3, Theme.accent);
		label(context, world, x + 122, centered(y + 66, 21, 10.5f), 10.5f, Theme.TEXT, Weight.SEMIBOLD);

		long active = Modules.ALL.stream().filter(module -> module.isEnabled() && module != Modules.CLICK_GUI).count();
		String[][] stats = {
			{Long.toString(active), "Активных модулей"},
			{Integer.toString(Modules.ALL.size() - 1), "Всего модулей"},
			{Integer.toString(Friends.NAMES.size()), "Друзей"}
		};
		float cardW = (w - 16) / 3f;
		for (int i = 0; i < stats.length; i++) {
			float cardX = x + i * (cardW + 8);
			Shapes.rect(context, cardX, y + 112, cardW, 66, 12, Theme.w02);
			Shapes.outline(context, cardX, y + 112, cardW, 66, 12, 1, Theme.w06);
			label(context, stats[i][0], cardX + 14, y + 124, 20, Theme.TEXT, Weight.SEMIBOLD);
			label(context, stats[i][1], cardX + 14, y + 152, 10.5f, Theme.muted, Weight.MEDIUM);
		}
	}

	// ---------------------------------------------------------------- themes

	private void drawThemes(DrawContext context) {
		float x = THEMES_X, y = THEMES_Y;
		Shapes.rect(context, x, y, THEMES_W, THEMES_H, 14, Theme.background | 0xF5000000);
		Shapes.outline(context, x, y, THEMES_W, THEMES_H, 14, 1, Theme.w08);
		// Swallow clicks so cards below the popup do not react.
		hit(x, y, THEMES_W, THEMES_H, button -> {
		});

		Shapes.circle(context, x + 22, y + 24, 5, Theme.accent);
		label(context, "Темы", x + 34, y + 15, 14, Theme.TEXT, Weight.SEMIBOLD);
		float closeX = x + THEMES_W - 16 - 24;
		Shapes.rect(context, closeX, y + 12, 24, 24, 7, Theme.w03);
		Shapes.outline(context, closeX, y + 12, 24, 24, 7, 1, Theme.w06);
		Icons.close(context, closeX + 6, y + 18, 12, Theme.textSoft);
		hit(closeX, y + 12, 24, 24, button -> themesOpen = false);

		float listY = y + 52, listH = THEMES_H - 52 - 10;
		float cardW = (THEMES_W - 18 - 14) / 3f, cardH = 57;
		int rows = (Theme.PALETTES.size() + 2) / 3;
		themesScroll = Math.clamp(themesScroll, 0, Math.max(0, rows * (cardH + 7) - 7 - listH));
		Shapes.pushClip(context, (int) x + 2, (int) listY, (int) (x + THEMES_W) - 2, (int) (listY + listH));
		for (int i = 0; i < Theme.PALETTES.size(); i++) {
			Theme.Palette palette = Theme.PALETTES.get(i);
			float cardX = x + 9 + i % 3 * (cardW + 7);
			float cardY = listY + i / 3 * (cardH + 7) - themesScroll;
			if (cardY + cardH < listY || cardY > listY + listH) continue;
			boolean selected = i == Theme.index();
			boolean hover = hovered(cardX, cardY, cardW, cardH) && hovered(x, listY, THEMES_W, listH);
			int index = i;
			Shapes.rect(context, cardX, cardY, cardW, cardH, 10, selected || hover ? Theme.w03 : Theme.w01);
			Shapes.rect(context, cardX, cardY, cardW, 34, 10, 10, 0, 0, Theme.mix(palette.accent() | 0xFF000000, 0xFFFFFFFF, 0.28f), palette.accent() | 0xFF000000, false);
			Shapes.outline(context, cardX, cardY, cardW, cardH, 10, 1, selected ? Theme.alpha(Theme.accent, 0.55f) : Theme.w06);
			float nameX = cardX + 7;
			if (selected) {
				Shapes.circle(context, nameX + 2.5f, cardY + 45.5f, 2.5f, Theme.accent);
				nameX += 10;
			}
			label(context, palette.name(), nameX, centered(cardY + 34, 23, 9.5f), 9.5f, Theme.TEXT, Weight.SEMIBOLD);
			if (cardY + cardH > listY && cardY < listY + listH) {
				float top = Math.max(cardY, listY), bottom = Math.min(cardY + cardH, listY + listH);
				hit(cardX, top, cardW, bottom - top, button -> Theme.select(index));
			}
		}
		Shapes.popClip(context);
	}

	// ---------------------------------------------------------------- input

	static String keyName(int key) {
		if (key < 0) return "n/a";
		return switch (key) {
			case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
			case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
			case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
			case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
			case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
			case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
			case GLFW.GLFW_KEY_SPACE -> "SPACE";
			case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
			default -> {
				String name = InputUtil.Type.KEYSYM.createFromCode(key).getLocalizedText().getString().toUpperCase(Locale.ROOT);
				yield name.length() > 8 ? name.substring(0, 8) : name;
			}
		};
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		float x = (float) (click.x() - originX) / scale;
		float y = (float) (click.y() - originY) / scale;
		// Any click ends a pending bind or search focus; the clicked region may start a new one.
		binding = null;
		searchFocused = false;

		boolean inViewport = x >= VIEW_X && x < VIEW_X + VIEW_W && y >= VIEW_Y && y < VIEW_Y + VIEW_H;
		// Later regions are drawn on top, so they win.
		for (int i = hits.size() - 1; i >= 0; i--) {
			Hit hit = hits.get(i);
			if (hit.inViewport && !inViewport) continue;
			if (x < hit.x || x >= hit.x + hit.w || y < hit.y || y >= hit.y + hit.h) continue;
			hit.action.accept(click.button());
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseDragged(Click click, double offsetX, double offsetY) {
		if (sliding == null) return super.mouseDragged(click, offsetX, offsetY);
		float x = (float) (click.x() - originX) / scale;
		sliding.setProgress((x - sliderX) / sliderW);
		return true;
	}

	@Override
	public boolean mouseReleased(Click click) {
		sliding = null;
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (overThemes()) themesScroll -= (float) verticalAmount * 32;
		else if (view == View.MODULES) scrollTarget = Math.clamp(scrollTarget - (float) verticalAmount * 56, 0, maxScroll);
		return true;
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		int key = input.key();
		// The character event of a key consumed as a bind must not start a search.
		skipChar = binding != null;
		if (binding != null) {
			if (key == GLFW.GLFW_KEY_ESCAPE) {
				binding = null;
			} else if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
				// The menu itself must stay reachable.
				if (binding != Modules.CLICK_GUI) binding.setKey(-1);
				binding = null;
			} else {
				binding.setKey(key);
				binding = null;
			}
			return true;
		}
		if (searchFocused) {
			if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) searchFocused = false;
			else if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) search = search.substring(0, search.length() - 1);
			return true;
		}
		if (key == GLFW.GLFW_KEY_ESCAPE && themesOpen) {
			themesOpen = false;
			return true;
		}
		if (key == Modules.CLICK_GUI.getKey()) {
			if (openingKeyPending) {
				openingKeyPending = false;
				return true;
			}
			close();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public boolean charTyped(CharInput input) {
		if (skipChar) {
			skipChar = false;
			return true;
		}
		if (binding != null || !input.isValidChar()) return false;
		// Typing anywhere starts a search, like in the reference menu.
		if (!searchFocused) {
			if (Character.isWhitespace(input.codepoint())) return false;
			searchFocused = true;
		}
		if (search.length() < 30) {
			search += input.asString();
			view = View.MODULES;
			scroll = scrollTarget = 0;
		}
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
