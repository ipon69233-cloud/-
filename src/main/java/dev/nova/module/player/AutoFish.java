package dev.nova.module.player;

import dev.nova.mixin.FishingBobberEntityAccessor;
import dev.nova.module.Category;
import dev.nova.module.Module;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoFish extends Module {
	private static final int RECAST_DELAY = 15;

	private int timer;

	public AutoFish() {
		super("AutoFish", "Сам подсекает рыбу и закидывает удочку", Category.PLAYER);
	}

	@Override
	protected void onEnable() {
		timer = RECAST_DELAY;
	}

	@Override
	public void onTick() {
		if (!mc.player.getMainHandStack().isOf(Items.FISHING_ROD) || mc.currentScreen != null) return;
		if (timer > 0) {
			timer--;
			return;
		}

		if (mc.player.fishHook == null) {
			useRod();
			timer = RECAST_DELAY;
		} else if (((FishingBobberEntityAccessor) mc.player.fishHook).nova$hasCaughtFish()) {
			useRod();
			timer = RECAST_DELAY;
		}
	}

	private static void useRod() {
		mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
		mc.player.swingHand(Hand.MAIN_HAND);
	}
}
