package dev.nova.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nova.module.Modules;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
	/** The server derives fall damage from the on-ground flag of movement packets. */
	@ModifyExpressionValue(
		method = "sendMovementPackets",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isOnGround()Z")
	)
	private boolean nova$spoofOnGround(boolean onGround) {
		ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
		if (self.isGliding()) return onGround;
		// Flying is client-side only here, so the server would otherwise count every descent as a fall.
		if (Modules.FLY.isEnabled() && self.getAbilities().flying) return true;
		if (Modules.NO_FALL.isEnabled() && Modules.NO_FALL.shouldSpoofGround()) return true;
		return onGround;
	}

	@ModifyExpressionValue(
		method = "applyMovementSpeedFactors",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")
	)
	private boolean nova$noSlow(boolean usingItem) {
		return usingItem && !Modules.NO_SLOW.isEnabled();
	}
}
