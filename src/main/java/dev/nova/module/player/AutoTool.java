package dev.nova.module.player;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoTool extends Module {
	private final BoolSetting restore = bool("Возврат в слот", true);

	/** Slot that was selected before the tool was picked, or -1. */
	private int previousSlot = -1;

	public AutoTool() {
		super("AutoTool", "Авто-выбор нужного инструмента", Category.PLAYER);
	}

	@Override
	protected void onDisable() {
		if (inGame()) restoreSlot();
	}

	private void restoreSlot() {
		if (previousSlot != -1 && restore.get()) mc.player.getInventory().setSelectedSlot(previousSlot);
		previousSlot = -1;
	}

	@Override
	public void onTick() {
		boolean mining = mc.options.attackKey.isPressed() && mc.currentScreen == null && !mc.player.isCreative()
			&& mc.crosshairTarget instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK;
		if (!mining) {
			restoreSlot();
			return;
		}

		BlockState state = mc.world.getBlockState(((BlockHitResult) mc.crosshairTarget).getBlockPos());
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
		if (best == inventory.getSelectedSlot()) return;
		if (previousSlot == -1) previousSlot = inventory.getSelectedSlot();
		inventory.setSelectedSlot(best);
	}
}
