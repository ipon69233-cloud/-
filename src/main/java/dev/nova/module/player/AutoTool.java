package dev.nova.module.player;

import dev.nova.module.Category;
import dev.nova.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoTool extends Module {
	public AutoTool() {
		super("AutoTool", "Берёт лучший инструмент для блока", Category.PLAYER);
	}

	@Override
	public void onTick() {
		if (!mc.options.attackKey.isPressed() || mc.currentScreen != null || mc.player.isCreative()) return;
		if (!(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;

		BlockState state = mc.world.getBlockState(hit.getBlockPos());
		PlayerInventory inventory = mc.player.getInventory();
		int best = inventory.getSelectedSlot();
		float bestSpeed = inventory.getStack(best).getMiningSpeedMultiplier(state);
		for (int slot = 0; slot < 9; slot++) {
			float speed = inventory.getStack(slot).getMiningSpeedMultiplier(state);
			if (speed > bestSpeed) {
				bestSpeed = speed;
				best = slot;
			}
		}
		if (best != inventory.getSelectedSlot()) inventory.setSelectedSlot(best);
	}
}
