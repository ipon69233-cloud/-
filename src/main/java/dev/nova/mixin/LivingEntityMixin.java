package dev.nova.mixin;

import dev.nova.module.Modules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
	private void nova$jesus(FluidState state, CallbackInfoReturnable<Boolean> cir) {
		if (Modules.JESUS.isEnabled() && Modules.JESUS.shouldWalkOnFluid((LivingEntity) (Object) this)) cir.setReturnValue(true);
	}
}
