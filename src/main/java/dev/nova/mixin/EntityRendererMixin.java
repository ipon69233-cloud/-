package dev.nova.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nova.module.Modules;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	/** NameTags draws its own tag for these players. */
	@WrapOperation(
		method = "updateRenderState",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;hasLabel(Lnet/minecraft/entity/Entity;D)Z")
	)
	private boolean nova$hideVanillaLabel(EntityRenderer<?, ?> renderer, Entity entity, double squaredDistance, Operation<Boolean> original) {
		return !Modules.NAME_TAGS.replaces(entity) && original.call(renderer, entity, squaredDistance);
	}
}
