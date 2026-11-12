package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.util.InvUtil;
import net.minecraft.item.Items;

public class AutoTotem extends Module {
	private final NumberSetting threshold = number("Порог ХП", 10, 0, 10, 0.1);
	private final BoolSetting fromInventory = bool("Свап из инвентаря", true);

	public AutoTotem() {
		super("AutoTotem", "Мгновенно ставит тотем при низком ХП", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;
		if (mc.player.getHealth() / 2f > threshold.get()) return;
		// Slot ids differ while a container is open, so wait until it is closed.
		if (mc.player.currentScreenHandler != mc.player.playerScreenHandler) return;

		int slot = fromInventory.get()
			? InvUtil.findInventory(stack -> stack.isOf(Items.TOTEM_OF_UNDYING))
			: InvUtil.findHotbar(stack -> stack.isOf(Items.TOTEM_OF_UNDYING));
		if (slot != -1) InvUtil.swapToOffhand(slot);
	}
}
