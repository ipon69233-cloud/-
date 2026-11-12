package dev.nova.module.world;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import dev.nova.setting.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Nuker extends Module {
	private static final int CREATIVE_BLOCKS_PER_TICK = 8;

	private final NumberSetting range = number("Радиус", 4.0, 1.0, 5.0, 0.5);
	private final BoolSetting flat = bool("Не копать ниже", true);

	/** Block being mined in survival; kept until it breaks so progress is not reset. */
	private BlockPos target;

	public Nuker() {
		super("Nuker", "Ломает блоки вокруг игрока", Category.MISC);
	}

	@Override
	protected void onDisable() {
		target = null;
	}

	@Override
	public void onTick() {
		if (mc.currentScreen != null || mc.player.isSpectator()) return;
		if (target != null && !isValid(target)) target = null;

		if (mc.player.isCreative()) {
			int broken = 0;
			for (BlockPos pos : nearby()) {
				if (!isValid(pos)) continue;
				mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
				if (++broken == CREATIVE_BLOCKS_PER_TICK) break;
			}
			if (broken > 0) mc.player.swingHand(Hand.MAIN_HAND);
			return;
		}

		if (target == null) target = findNearest();
		if (target != null && mc.interactionManager.updateBlockBreakingProgress(target, Direction.UP)) {
			mc.player.swingHand(Hand.MAIN_HAND);
		}
	}

	private Iterable<BlockPos> nearby() {
		BlockPos feet = mc.player.getBlockPos();
		int radius = (int) Math.ceil(range.get());
		return BlockPos.iterate(feet.add(-radius, -radius, -radius), feet.add(radius, radius, radius));
	}

	private BlockPos findNearest() {
		Vec3d eyes = mc.player.getEyePos();
		BlockPos nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		for (BlockPos pos : nearby()) {
			double distance = eyes.squaredDistanceTo(Vec3d.ofCenter(pos));
			if (distance < nearestDistance && isValid(pos)) {
				nearestDistance = distance;
				nearest = pos.toImmutable();
			}
		}
		return nearest;
	}

	private boolean isValid(BlockPos pos) {
		if (flat.get() && pos.getY() < mc.player.getBlockPos().getY()) return false;
		if (mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos)) > range.get() * range.get()) return false;
		BlockState state = mc.world.getBlockState(pos);
		if (state.isAir() || state.getBlock() instanceof FluidBlock) return false;
		return state.getHardness(mc.world, pos) >= 0;
	}

	@Override
	public String getSuffix() {
		return range.format();
	}
}
