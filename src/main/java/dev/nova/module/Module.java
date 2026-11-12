package dev.nova.module;

import dev.nova.setting.BoolSetting;
import dev.nova.setting.ColorSetting;
import dev.nova.setting.ModeSetting;
import dev.nova.setting.MultiSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
	protected static final MinecraftClient mc = MinecraftClient.getInstance();

	private final String name;
	private final String description;
	private final Category category;
	private final List<Setting<?>> settings = new ArrayList<>();
	// Read from chunk-building and network threads by mixins.
	private volatile boolean enabled;
	private int key = -1;
	/** Active only while the key is held instead of toggling. */
	protected boolean hold;
	/** Hidden from the on-screen module list. */
	protected boolean hidden;

	protected Module(String name, String description, Category category) {
		this.name = name;
		this.description = description;
		this.category = category;
	}

	protected BoolSetting bool(String name, boolean value) {
		return add(new BoolSetting(name, value));
	}

	protected NumberSetting number(String name, double value, double min, double max, double step) {
		return add(new NumberSetting(name, value, min, max, step));
	}

	protected ModeSetting mode(String name, String value, String... modes) {
		return add(new ModeSetting(name, value, modes));
	}

	/** Items are declared on the result with {@link MultiSetting#add}. */
	protected MultiSetting multi(String name) {
		return add(new MultiSetting(name));
	}

	protected ColorSetting color(String name, int color) {
		return add(new ColorSetting(name, color));
	}

	private <S extends Setting<?>> S add(S setting) {
		settings.add(setting);
		return setting;
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) return;
		this.enabled = enabled;
		if (enabled) onEnable();
		else onDisable();
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	public void onKey(boolean pressed) {
		if (hold) setEnabled(pressed);
		else if (pressed) toggle();
	}

	protected static boolean inGame() {
		return mc.player != null && mc.world != null;
	}

	/** Sets the client-side base value of one of the player's attributes. */
	protected static void setAttribute(RegistryEntry<EntityAttribute> attribute, double value) {
		EntityAttributeInstance instance = mc.player.getAttributeInstance(attribute);
		if (instance != null && instance.getBaseValue() != value) instance.setBaseValue(value);
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}

	/** Called every client tick while enabled and in a world. */
	public void onTick() {
	}

	/** Called every frame on top of the HUD while enabled and in a world. */
	public void onRender2D(DrawContext context, float tickDelta) {
	}

	/** Short text shown next to the name in the module list. */
	public String getSuffix() {
		return null;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Category getCategory() {
		return category;
	}

	public List<Setting<?>> getSettings() {
		return settings;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public boolean isHold() {
		return hold;
	}

	public boolean isHidden() {
		return hidden;
	}
}
