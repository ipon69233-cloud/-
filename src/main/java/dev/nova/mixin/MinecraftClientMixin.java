package dev.nova.mixin;

import dev.nova.NovaClient;
import dev.nova.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	private void nova$onTick(CallbackInfo ci) {
		NovaClient.onTick();
	}

	@Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
	private void nova$espOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (Modules.ESP.shouldGlow(entity)) cir.setReturnValue(true);
	}

	/** Ambient occlusion would shade ores that are enclosed by hidden blocks almost black. */
	@Inject(method = "isAmbientOcclusionEnabled", at = @At("HEAD"), cancellable = true)
	private static void nova$xrayNoAmbientOcclusion(CallbackInfoReturnable<Boolean> cir) {
		if (Modules.XRAY.isEnabled()) cir.setReturnValue(false);
	}
}
