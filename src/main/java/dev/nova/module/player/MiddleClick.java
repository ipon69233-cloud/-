package dev.nova.module.player;

import dev.nova.config.Config;
import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ModeSetting;
import dev.nova.util.Friends;
import dev.nova.util.InvUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

/** Hooked into the middle click in MinecraftClientMixin. */
public class MiddleClick extends Module {
	private final ModeSetting action = mode("Действие", "Друг", "Друг", "Жемчуг");

	public MiddleClick() {
		super("MiddleClick", "Действие по средней кнопке мыши", Category.PLAYER);
	}

	/** Returns true when the click was used and the vanilla pick-block action should be skipped. */
	public boolean onMiddleClick() {
		if (mc.currentScreen != null || mc.player == null) return false;
		return action.is("Друг") ? toggleFriend() : throwPearl();
	}

	private boolean toggleFriend() {
		if (!(mc.crosshairTarget instanceof EntityHitResult hit) || !(hit.getEntity() instanceof PlayerEntity player)) return false;
		boolean added = Friends.toggle(player);
		String name = player.getGameProfile().name();
		mc.inGameHud.getChatHud().addMessage(Text.literal(added ? "§7Друг добавлен: §a" + name : "§7Друг удалён: §c" + name));
		Config.save();
		return true;
	}

	private boolean throwPearl() {
		int slot = InvUtil.findHotbar(stack -> stack.isOf(Items.ENDER_PEARL));
		if (slot == -1) return false;

		PlayerInventory inventory = mc.player.getInventory();
		int previous = inventory.getSelectedSlot();
		inventory.setSelectedSlot(slot);
		mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
		mc.player.swingHand(Hand.MAIN_HAND);
		inventory.setSelectedSlot(previous);
		return true;
	}
}
