package dev.nova.module.player;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.util.InvUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class ElytraHelper extends Module {
	// Horizontal speed in blocks per tick under which a rocket is used.
	private static final double LOW_SPEED = 0.9;

	private final BoolSetting autoFirework = bool("Авто-фейерверк", true);
	private final NumberSetting delay = number("Задержка", 40, 10, 100, 5);

	private int cooldown;

	public ElytraHelper() {
		super("ElytraHelper", "Помощник полёта на элитрах", Category.PLAYER);
	}

	@Override
	protected void onEnable() {
		cooldown = 0;
	}

	@Override
	public void onTick() {
		if (cooldown > 0) cooldown--;
		if (!autoFirework.get() || cooldown > 0 || !mc.player.isGliding() || mc.currentScreen != null) return;
		if (mc.player.getVelocity().horizontalLength() >= LOW_SPEED) return;

		int slot = InvUtil.findHotbar(stack -> stack.isOf(Items.FIREWORK_ROCKET));
		if (slot == -1) return;

		PlayerInventory inventory = mc.player.getInventory();
		int previous = inventory.getSelectedSlot();
		inventory.setSelectedSlot(slot);
		mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
		inventory.setSelectedSlot(previous);
		cooldown = delay.getInt();
	}
}
