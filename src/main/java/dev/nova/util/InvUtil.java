package dev.nova.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.function.Predicate;

public final class InvUtil {
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	public static final int OFFHAND_BUTTON = 40;

	private InvUtil() {
	}

	/** Hotbar index 0-8 of the first matching stack, or -1. */
	public static int findHotbar(Predicate<ItemStack> filter) {
		PlayerInventory inventory = mc.player.getInventory();
		for (int i = 0; i < 9; i++) {
			if (filter.test(inventory.getStack(i))) return i;
		}
		return -1;
	}

	/** Inventory index 0-35 of the first matching stack, or -1. */
	public static int findInventory(Predicate<ItemStack> filter) {
		PlayerInventory inventory = mc.player.getInventory();
		for (int i = 0; i < 36; i++) {
			if (filter.test(inventory.getStack(i))) return i;
		}
		return -1;
	}

	/** Swaps an inventory stack with the offhand. Only valid while no container is open. */
	public static void swapToOffhand(int inventoryIndex) {
		int slotId = inventoryIndex < 9 ? inventoryIndex + 36 : inventoryIndex;
		mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slotId, OFFHAND_BUTTON, SlotActionType.SWAP, mc.player);
	}
}
