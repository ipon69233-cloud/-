package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import dev.nova.util.InvUtil;
import dev.nova.util.KeyUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class AutoGApple extends Module {
	private static final int MAX_EAT_TICKS = 80;

	private final NumberSetting threshold = number("Порог ХП", 8, 0, 20, 0.5);

	private boolean eating;
	private boolean started;
	private int ticks;
	private int previousSlot;

	public AutoGApple() {
		super("AutoGApple", "Авто-съедение голдэпла при уроне", Category.COMBAT);
	}

	private static boolean isGoldenApple(ItemStack stack) {
		return stack.isOf(Items.GOLDEN_APPLE) || stack.isOf(Items.ENCHANTED_GOLDEN_APPLE);
	}

	@Override
	public void onTick() {
		PlayerInventory inventory = mc.player.getInventory();

		if (eating) {
			started |= mc.player.isUsingItem();
			boolean done = started && !mc.player.isUsingItem();
			if (done || ++ticks > MAX_EAT_TICKS || !isGoldenApple(mc.player.getMainHandStack())) stop();
			else KeyUtil.holdUse(true);
			return;
		}
		if (mc.player.getHealth() > threshold.get() || mc.currentScreen != null || mc.player.isUsingItem()) return;

		int slot = InvUtil.findHotbar(AutoGApple::isGoldenApple);
		if (slot == -1) return;
		previousSlot = inventory.getSelectedSlot();
		inventory.setSelectedSlot(slot);
		eating = true;
		started = false;
		ticks = 0;
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
