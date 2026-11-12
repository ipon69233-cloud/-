package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

/** The field of view is narrowed in GameRendererMixin. */
public class Zoom extends Module {
	private final NumberSetting factor = number("Кратность", 4.0, 2.0, 16.0, 1.0);

	public Zoom() {
		super("Zoom", "Приближает камеру, пока зажата клавиша", Category.VISUALS);
		hold = true;
		hidden = true;
		setKey(GLFW.GLFW_KEY_C);
	}

	public float apply(float fov) {
		return fov / factor.getFloat();
	}
}
