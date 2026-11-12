package dev.nova.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.nova.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
	private void nova$cancelKnockback(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (Modules.VELOCITY.isEnabled() && player != null && packet.getEntityId() == player.getId()) ci.cancel();
	}

	@WrapWithCondition(
		method = "onExplosion",
		at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V")
	)
	private boolean nova$cancelExplosionKnockback(Optional<?> knockback, Consumer<?> action) {
		return !Modules.VELOCITY.isEnabled();
	}
}
