package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;
import net.minecraft.item.Items;

public class BowHelper extends Module {
	private final NumberSetting draw = number("Натяжение", 20, 5, 20, 1);

	public BowHelper() {
		super("BowHelper", "Автонатяжение лука", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (!mc.player.isUsingItem() || !mc.player.getActiveItem().isOf(Items.BOW)) return;
		if (mc.player.getItemUseTime() >= draw.getInt()) mc.interactionManager.stopUsingItem(mc.player);
	}

	@Override
	public String getSuffix() {
		return draw.format();
	}
}
