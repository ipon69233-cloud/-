package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.util.InvUtil;
import net.minecraft.item.Items;

public class AutoTotem extends Module {
	public AutoTotem() {
		super("AutoTotem", "Держит тотем бессмертия во второй руке", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;
		// Slot ids differ while a container is open, so wait until it is closed.
		if (mc.player.currentScreenHandler != mc.player.playerScreenHandler) return;

		int slot = InvUtil.findInventory(stack -> stack.isOf(Items.TOTEM_OF_UNDYING));
		if (slot != -1) InvUtil.swapToOffhand(slot);
	}
}
