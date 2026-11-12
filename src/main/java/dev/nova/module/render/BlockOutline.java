package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ColorSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.util.WorldOverlay;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

/** The vanilla outline is suppressed in GameRendererMixin. */
public class BlockOutline extends Module {
	private final NumberSetting thickness = number("Толщина", 1.5, 0.5, 4, 0.1);
	private final ColorSetting color = color("Цвет", 0xFFFFFFFF);

	public BlockOutline() {
		super("BlockOutline", "Обводка выделенного блока", Category.VISUALS);
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		if (mc.options.hudHidden || !(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;

		BlockPos pos = hit.getBlockPos();
		BlockState state = mc.world.getBlockState(pos);
		if (state.isAir()) return;
		VoxelShape shape = state.getOutlineShape(mc.world, pos);
		if (shape.isEmpty()) return;

		WorldOverlay.Lines lines = WorldOverlay.begin(context);
		for (Box box : shape.getBoundingBoxes()) {
			lines.box(box.offset(pos).expand(0.002), color.get(), thickness.getFloat());
		}
		lines.submit(context);
	}
}
