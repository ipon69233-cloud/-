package dev.nova.util;

import dev.nova.mixin.KeyBindingAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class KeyUtil {
	private static final MinecraftClient mc = MinecraftClient.getInstance();

	private KeyUtil() {
	}

	/** Whether the physical key or mouse button bound to the binding is down right now. */
	public static boolean isDown(KeyBinding binding) {
		InputUtil.Key key = ((KeyBindingAccessor) binding).nova$getBoundKey();
		return switch (key.getCategory()) {
			case KEYSYM -> key.getCode() != InputUtil.UNKNOWN_KEY.getCode() && InputUtil.isKeyPressed(mc.getWindow(), key.getCode());
			case MOUSE -> GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), key.getCode()) == GLFW.GLFW_PRESS;
			default -> false;
		};
	}

	/** Puts the binding back to what the keyboard says, for modules that held it down. */
	public static void release(KeyBinding binding) {
		binding.setPressed(isDown(binding));
	}

	/** The use key may be in toggle mode, where every press flips its state. */
	public static void holdUse(boolean pressed) {
		KeyBinding use = mc.options.useKey;
		if (use.isPressed() == pressed) return;
		use.setPressed(pressed);
		if (use.isPressed() != pressed) use.setPressed(true);
	}
}
