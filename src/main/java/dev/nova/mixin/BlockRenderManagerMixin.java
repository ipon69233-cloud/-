package dev.nova.mixin;

import dev.nova.module.Modules;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Runs on the chunk meshing threads. */
@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {
	@Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
	private void nova$xrayBlock(
		BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer,
		boolean cull, List<BlockModelPart> parts, CallbackInfo ci
	) {
		if (Modules.XRAY.isEnabled() && !Modules.XRAY.isVisible(state)) ci.cancel();
	}

	/** Visible blocks keep every face, even those touching hidden neighbours. */
	@ModifyVariable(method = "renderBlock", at = @At("HEAD"), argsOnly = true)
	private boolean nova$xrayNoCulling(boolean cull) {
		return cull && !Modules.XRAY.isEnabled();
	}

	@Inject(method = "renderFluid", at = @At("HEAD"), cancellable = true)
	private void nova$xrayFluid(BlockPos pos, BlockRenderView world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
		if (Modules.XRAY.isEnabled() && !Modules.XRAY.isVisible(fluidState)) ci.cancel();
	}
}
