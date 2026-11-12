package dev.nova.mixin;

import dev.nova.module.Modules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
	private void nova$jesus(FluidState state, CallbackInfoReturnable<Boolean> cir) {
		if (Modules.JESUS.isEnabled() && Modules.JESUS.shouldWalkOnFluid((LivingEntity) (Object) this)) cir.setReturnValue(true);
	}

	/** DragonFly only takes over for the local player, the module checks the entity. */
	@Inject(method = "travelGliding", at = @At("HEAD"), cancellable = true)
	private void nova$dragonFly(Vec3d movementInput, CallbackInfo ci) {
		if (Modules.DRAGON_FLY.isEnabled() && Modules.DRAGON_FLY.glide((LivingEntity) (Object) this)) ci.cancel();
	}
}
