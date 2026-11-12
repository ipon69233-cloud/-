package dev.nova.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nova.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void nova$zoom(Camera camera, float tickProgress, boolean changingFov, CallbackInfoReturnable<Float> cir) {
		// The hand is rendered with changingFov = false and should keep its size.
		if (changingFov && Modules.ZOOM.isEnabled()) cir.setReturnValue(Modules.ZOOM.apply(cir.getReturnValue()));
	}

	/** The death roll shares this method and stays. */
	@Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
	private void nova$noHurtTilt(MatrixStack matrices, float tickProgress, CallbackInfo ci) {
		if (Modules.REMOVALS.removes("Тряску") && MinecraftClient.getInstance().getCameraEntity() instanceof LivingEntity entity && !entity.isDead()) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(
		method = "renderWorld",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;shouldRenderBlockOutline()Z")
	)
	private boolean nova$replaceBlockOutline(boolean render) {
		return render && !Modules.BLOCK_OUTLINE.isEnabled();
	}

	/** The detached camera has no hand to show. */
	@ModifyExpressionValue(
		method = "renderHand",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z")
	)
	private boolean nova$hideHand(boolean firstPerson) {
		return firstPerson && !Modules.FREE_CAMERA.isEnabled();
	}
}
