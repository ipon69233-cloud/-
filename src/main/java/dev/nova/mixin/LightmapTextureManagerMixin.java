package dev.nova.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nova.module.render.Fullbright;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
	/** The second float option read while building the lightmap is the gamma. */
	@ModifyExpressionValue(
		method = "update",
		at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1)
	)
	private float nova$fullbright(float gamma) {
		return Fullbright.isBright() ? Fullbright.BRIGHT_GAMMA : gamma;
	}
}
