package dev.nova.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nova.module.Modules;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shared with the integrated server, so every hook only acts for client-side entities. */
@Mixin(Entity.class)
public class EntityMixin {
	/** The glow outline takes its color from the team color. */
	@Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
	private void nova$glowColor(CallbackInfoReturnable<Integer> cir) {
		Entity self = (Entity) (Object) this;
		if (self.getEntityWorld().isClient() && Modules.GLOW_ESP.shouldGlow(self)) cir.setReturnValue(Modules.GLOW_ESP.colorOf());
	}

	@ModifyReturnValue(method = "getTargetingMargin", at = @At("RETURN"))
	private float nova$hitBox(float margin) {
		Entity self = (Entity) (Object) this;
		if (!Modules.HIT_BOX.isEnabled() || !self.getEntityWorld().isClient()) return margin;
		return margin + Modules.HIT_BOX.margin(self);
	}

	@Inject(method = "move", at = @At("HEAD"), cancellable = true)
	private void nova$airStuck(MovementType type, Vec3d movement, CallbackInfo ci) {
		if (Modules.AIR_STUCK.isEnabled() && Modules.AIR_STUCK.freezes((Entity) (Object) this)) ci.cancel();
	}

	@Inject(method = "slowMovement", at = @At("HEAD"), cancellable = true)
	private void nova$noWeb(BlockState state, Vec3d multiplier, CallbackInfo ci) {
		if (Modules.NO_WEB.isEnabled() && state.isOf(Blocks.COBWEB) && Modules.NO_WEB.ignores((Entity) (Object) this)) ci.cancel();
	}

	@Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
	private void nova$noPush(Entity other, CallbackInfo ci) {
		if (Modules.NO_PUSH.isEnabled() && Modules.NO_PUSH.ignoresEntities() && (Object) this == MinecraftClient.getInstance().player) ci.cancel();
	}

	/** The mouse turns the free camera instead of the player. */
	@Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
	private void nova$freeCameraLook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
		if (Modules.FREE_CAMERA.isEnabled() && (Object) this == MinecraftClient.getInstance().player) {
			Modules.FREE_CAMERA.rotate(cursorDeltaX, cursorDeltaY);
			ci.cancel();
		}
	}

	/** Invisible entities are drawn as translucent ghosts instead of being skipped. */
	@ModifyReturnValue(method = "isInvisibleTo", at = @At("RETURN"))
	private boolean nova$seeInvisibles(boolean invisible) {
		return invisible && !(Modules.SEE_INVISIBLES.isEnabled() && ((Entity) (Object) this).getEntityWorld().isClient());
	}
}
