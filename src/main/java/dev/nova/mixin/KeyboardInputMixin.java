package dev.nova.mixin;

import dev.nova.module.Modules;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
	/** The movement keys move the free camera, the player itself stands still. */
	@Inject(method = "tick", at = @At("TAIL"))
	private void nova$freeCameraInput(CallbackInfo ci) {
		if (!Modules.FREE_CAMERA.isEnabled()) return;
		this.playerInput = PlayerInput.DEFAULT;
		this.movementVector = Vec2f.ZERO;
	}
}
