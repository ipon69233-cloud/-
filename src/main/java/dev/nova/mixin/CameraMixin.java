package dev.nova.mixin;

import dev.nova.module.Modules;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	private boolean thirdPerson;

	@Shadow
	protected abstract void setRotation(float yaw, float pitch);

	@Shadow
	protected abstract void setPos(double x, double y, double z);

	/** Replaces whatever the vanilla update decided; the camera still follows the player entity otherwise. */
	@Inject(method = "update", at = @At("TAIL"))
	private void nova$freeCamera(World area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
		if (!Modules.FREE_CAMERA.isEnabled()) return;
		Modules.FREE_CAMERA.step();
		// Makes the world renderer draw the player model.
		this.thirdPerson = true;
		this.setRotation(Modules.FREE_CAMERA.getYaw(), Modules.FREE_CAMERA.getPitch());
		this.setPos(Modules.FREE_CAMERA.getPos().x, Modules.FREE_CAMERA.getPos().y, Modules.FREE_CAMERA.getPos().z);
	}

	/** The distance is the unclipped one when blocks should not pull the camera closer. */
	@Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
	private void nova$cameraClip(float distance, CallbackInfoReturnable<Float> cir) {
		if (Modules.CAMERA_CLIP.isEnabled()) cir.setReturnValue(Modules.CAMERA_CLIP.getDistance());
	}
}
