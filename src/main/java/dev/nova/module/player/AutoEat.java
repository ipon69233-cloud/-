package dev.nova.module.player;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import dev.nova.util.InvUtil;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class AutoEat extends Module {
	private final NumberSetting hunger = number("Hunger", 14, 1, 19, 1);

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
			else hold(true);
			return;
		}
		if (!hungry || mc.currentScreen != null) return;

		int slot = InvUtil.findHotbar(AutoEat::isFood);
		if (slot == -1) return;
		previousSlot = inventory.getSelectedSlot();
		inventory.setSelectedSlot(slot);
		eating = true;
		hold(true);
	}

	private void stop() {
		eating = false;
		hold(false);
		if (inGame()) mc.player.getInventory().setSelectedSlot(previousSlot);
	}

	/** The use key may be in toggle mode, where every press flips its state. */
	private static void hold(boolean pressed) {
		KeyBinding use = mc.options.useKey;
		if (use.isPressed() == pressed) return;
		use.setPressed(pressed);
		if (use.isPressed() != pressed) use.setPressed(true);
	}

	@Override
	protected void onDisable() {
		if (eating) stop();
	}
}
