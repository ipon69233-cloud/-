package dev.nova.module.player;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import dev.nova.setting.NumberSetting;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class ChestStealer extends Module {
	private final NumberSetting delay = number("Delay", 1, 0, 10, 1);
	private final BoolSetting autoClose = bool("AutoClose", true);

	private int timer;

	public ChestStealer() {
		super("ChestStealer", "Забирает всё из открытого сундука", Category.PLAYER);
	}

	@Override
	public void onTick() {
		if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
			timer = 0;
			return;
		}
		if (timer > 0) {
			timer--;
			return;
		}

		GenericContainerScreenHandler handler = screen.getScreenHandler();
		int size = handler.getRows() * 9;
		for (int slot = 0; slot < size; slot++) {
			if (!handler.getSlot(slot).hasStack()) continue;
			mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.QUICK_MOVE, mc.player);
			if (delay.getInt() > 0) {
				timer = delay.getInt();
				return;
			}
		}
		// Reached only once nothing is left to take.
		if (autoClose.get()) mc.player.closeHandledScreen();
	}
}
