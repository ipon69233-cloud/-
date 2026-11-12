package dev.nova.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nova.module.Modules;
import net.minecraft.client.input.Input;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Input.class)
public class InputMixin {
	@Shadow
	protected Vec2f movementVector;

	/** Sprinting only starts and continues with forward input, so any movement counts as forward for it. */
	@ModifyReturnValue(method = "hasForwardMovement", at = @At("RETURN"))
	private boolean nova$omnidirectionalSprint(boolean forward) {
		return forward || Modules.SPRINT.isEnabled() && Modules.SPRINT.isOmnidirectional() && movementVector.lengthSquared() > 1.0E-5F;
	}
}
