package dev.nova.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nova.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@WrapOperation(
		method = "onEntityVelocityUpdate",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setVelocityClient(Lnet/minecraft/util/math/Vec3d;)V")
	)
	private void nova$scaleKnockback(Entity entity, Vec3d velocity, Operation<Void> original) {
		if (Modules.VELOCITY.isEnabled() && entity == MinecraftClient.getInstance().player) {
			velocity = Modules.VELOCITY.scaleVelocity(entity.getVelocity(), velocity);
		}
		original.call(entity, velocity);
	}

	@WrapOperation(
		method = "onExplosion",
		at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V")
	)
	private void nova$scaleExplosionKnockback(Optional<Vec3d> knockback, Consumer<Vec3d> action, Operation<Void> original) {
		original.call(Modules.VELOCITY.isEnabled() ? knockback.map(Modules.VELOCITY::scaleKnockback) : knockback, action);
	}
}
