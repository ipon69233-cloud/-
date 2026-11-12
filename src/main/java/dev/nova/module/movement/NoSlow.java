package dev.nova.module.movement;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.MultiSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** The slowdown is skipped in ClientPlayerEntityMixin. */
public class NoSlow extends Module {
	private final MultiSetting items = multi("Предметы").add("Еда", true).add("Лук", true).add("Щит", true).add("Зелья", true);

	public NoSlow() {
		super("NoSlow", "Убирает замедление от еды и щита", Category.MOVEMENT);
	}

	/** Whether using this stack should leave the movement speed alone. */
	public boolean ignores(ItemStack stack) {
		if (items.is("Еда") && stack.get(DataComponentTypes.FOOD) != null) return true;
		if (items.is("Лук") && (stack.isOf(Items.BOW) || stack.isOf(Items.CROSSBOW))) return true;
		if (items.is("Щит") && stack.get(DataComponentTypes.BLOCKS_ATTACKS) != null) return true;
		return items.is("Зелья") && stack.isOf(Items.POTION);
	}
}
