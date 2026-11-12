package dev.nova.mixin;

import dev.nova.module.Modules;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void nova$zoom(Camera camera, float tickProgress, boolean changingFov, CallbackInfoReturnable<Float> cir) {
		// The hand is rendered with changingFov = false and should keep its size.
		if (changingFov && Modules.ZOOM.isEnabled()) cir.setReturnValue(Modules.ZOOM.apply(cir.getReturnValue()));
	}
}
