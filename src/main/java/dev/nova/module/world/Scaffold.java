package dev.nova.module.world;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.util.InvUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {
	public Scaffold() {
		super("Scaffold", "Ставит блоки под ногами при ходьбе", Category.MISC);
	}

	private static boolean isBuildingBlock(ItemStack stack) {
		if (!(stack.getItem() instanceof BlockItem item)) return false;
		Block block = item.getBlock();
		return Block.isShapeFullCube(block.getDefaultState().getCollisionShape(mc.world, BlockPos.ORIGIN));
	}

	@Override
	public void onTick() {
		if (mc.currentScreen != null) return;
		BlockPos below = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.5, mc.player.getZ());
		if (!isReplaceable(below)) return;

		int slot = InvUtil.findHotbar(Scaffold::isBuildingBlock);
		if (slot == -1) return;

		PlayerInventory inventory = mc.player.getInventory();
		int previous = inventory.getSelectedSlot();
		inventory.setSelectedSlot(slot);
		if (!place(below)) {
			// Nothing to place against yet: extend from a neighbour that has support.
			for (Direction direction : Direction.Type.HORIZONTAL) {
				BlockPos side = below.offset(direction);
				if (isReplaceable(side) && place(side)) break;
			}
		}
		inventory.setSelectedSlot(previous);
	}

	private static boolean isReplaceable(BlockPos pos) {
		return mc.world.getBlockState(pos).isReplaceable();
	}

	/** Places the held block at pos by clicking an adjacent solid block. */
	private static boolean place(BlockPos pos) {
		for (Direction direction : Direction.values()) {
			BlockPos neighbour = pos.offset(direction);
			if (isReplaceable(neighbour)) continue;

			Direction face = direction.getOpposite();
			Vec3d hit = Vec3d.ofCenter(neighbour).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
			mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(hit, face, neighbour, false));
			mc.player.swingHand(Hand.MAIN_HAND);
			return true;
		}
		return false;
	}
}
