package dev.nova.mixin;

import dev.nova.module.Modules;
import net.minecraft.client.render.RenderTickCounter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
public class RenderTickCounterMixin {
	@Shadow
	private float dynamicDeltaTicks;

	/** Scales how many game ticks pass per real-time frame. */
	@Inject(
		method = "beginRenderTick(J)I",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;dynamicDeltaTicks:F",
			opcode = Opcodes.PUTFIELD,
			shift = At.Shift.AFTER
		)
	)
	private void nova$timer(long timeMillis, CallbackInfoReturnable<Integer> cir) {
		if (Modules.TIMER.isEnabled()) dynamicDeltaTicks *= Modules.TIMER.getMultiplier();
	}
}
