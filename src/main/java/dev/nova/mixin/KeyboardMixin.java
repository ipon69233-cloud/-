package dev.nova.mixin;

import dev.nova.NovaClient;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "onKey", at = @At("HEAD"))
	private void nova$onKey(long window, int action, KeyInput input, CallbackInfo ci) {
		if (window != client.getWindow().getHandle() || action == GLFW.GLFW_REPEAT) return;
		// Releases always pass so hold-to-use modules cannot get stuck behind a screen.
		boolean pressed = action == GLFW.GLFW_PRESS;
		if (pressed && client.currentScreen != null) return;
		NovaClient.onKey(input.key(), pressed);
	}
}
