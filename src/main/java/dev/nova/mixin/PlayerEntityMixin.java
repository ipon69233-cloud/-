package dev.nova.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nova.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@ModifyReturnValue(method = "isPushedByFluids", at = @At("RETURN"))
	private boolean nova$noFluidPush(boolean pushed) {
		if (!pushed || !Modules.NO_PUSH.isEnabled() || !Modules.NO_PUSH.ignoresFluids()) return pushed;
		return (Object) this != MinecraftClient.getInstance().player;
	}
}
