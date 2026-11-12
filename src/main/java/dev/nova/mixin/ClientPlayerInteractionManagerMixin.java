package dev.nova.mixin;

import dev.nova.module.Modules;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
	@Inject(method = "attackEntity", at = @At("HEAD"))
	private void nova$onAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
		if (Modules.CRITICALS.isEnabled()) Modules.CRITICALS.beforeAttack(target);
		if (Modules.HIT_SOUNDS.isEnabled()) Modules.HIT_SOUNDS.onAttack(target);
	}
}
