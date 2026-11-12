package dev.nova.mixin;

import dev.nova.module.Modules;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientCommonNetworkHandlerMixin {
	@Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
	private void nova$blink(Packet<?> packet, CallbackInfo ci) {
		if (Modules.BLINK.isEnabled() && Modules.BLINK.capture(packet)) ci.cancel();
	}
}
