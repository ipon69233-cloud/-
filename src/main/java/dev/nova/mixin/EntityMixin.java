package dev.nova.mixin;

import dev.nova.module.Modules;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
	/** The glow outline takes its color from the team color. */
	@Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
	private void nova$espColor(CallbackInfoReturnable<Integer> cir) {
		Entity self = (Entity) (Object) this;
		if (self.getEntityWorld().isClient() && Modules.ESP.shouldGlow(self)) cir.setReturnValue(Modules.ESP.colorOf(self));
	}
}
