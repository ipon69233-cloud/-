package dev.nova.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.nova.NovaClient;
import dev.nova.module.Modules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void nova$render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		NovaClient.onRender2D(context, tickCounter.getTickProgress(false));
	}

	/** The first overlay drawn there is the one of the worn pumpkin, powder snow comes after it. */
	@WrapWithCondition(
		method = "renderMiscOverlays",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V",
			ordinal = 0
		)
	)
	private boolean nova$keepPumpkinOverlay(InGameHud hud, DrawContext context, Identifier texture, float opacity) {
		return !Modules.REMOVALS.removes("Тыкву");
	}
}
