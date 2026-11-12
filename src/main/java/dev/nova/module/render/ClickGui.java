package dev.nova.module.render;

import dev.nova.gui.ClickGuiScreen;
import dev.nova.module.Category;
import dev.nova.module.Module;
import org.lwjgl.glfw.GLFW;

public class ClickGui extends Module {
	public ClickGui() {
		super("ClickGUI", "Открывает это меню", Category.RENDER);
		hidden = true;
		setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	@Override
	protected void onEnable() {
		mc.setScreen(new ClickGuiScreen());
		// Acts as a button rather than a toggle.
		setEnabled(false);
	}
}
