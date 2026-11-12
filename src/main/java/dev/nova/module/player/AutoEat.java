package dev.nova.module.player;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import dev.nova.util.InvUtil;
import dev.nova.util.KeyUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class AutoEat extends Module {
	private final NumberSetting hunger = number("Голод", 14, 1, 19, 1);

	private boolean eating;
	private int previousSlot;

	public AutoEat() {
		super("AutoEat", "Сам ест еду из хотбара, когда голоден", Category.PLAYER);
	}

	private static boolean isFood(ItemStack stack) {
		return stack.get(DataComponentTypes.FOOD) != null;
	}

	@Override
	public void onTick() {
		PlayerInventory inventory = mc.player.getInventory();
		boolean hungry = mc.player.getHungerManager().getFoodLevel() <= hunger.getInt();

		if (eating) {
			boolean done = !hungry && !mc.player.isUsingItem();
			if (done || !isFood(mc.player.getMainHandStack())) stop();
			else KeyUtil.holdUse(true);
			return;
		}
		if (!hungry || mc.currentScreen != null) return;

		int slot = InvUtil.findHotbar(AutoEat::isFood);
		if (slot == -1) return;
		previousSlot = inventory.getSelectedSlot();
		inventory.setSelectedSlot(slot);
		eating = true;
		KeyUtil.holdUse(true);
	}

	private void stop() {
		eating = false;
		KeyUtil.holdUse(false);
		if (inGame()) mc.player.getInventory().setSelectedSlot(previousSlot);
	}

	@Override
	protected void onDisable() {
		if (eating) stop();
	}
}
