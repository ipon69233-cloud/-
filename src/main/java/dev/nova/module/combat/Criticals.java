package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Module {
	public Criticals() {
		super("Criticals", "Каждый удар с земли становится критическим", Category.COMBAT);
	}

	/** Called right before the attack packet is sent. */
	public void beforeAttack(Entity target) {
		if (!(target instanceof LivingEntity) || !mc.player.isOnGround()) return;
		if (mc.player.isTouchingWater() || mc.player.isInLava() || mc.player.isClimbing() || mc.player.hasVehicle()) return;

		// A tiny hop reported to the server makes it treat the hit as a falling attack.
		double x = mc.player.getX();
		double y = mc.player.getY();
		double z = mc.player.getZ();
		boolean collision = mc.player.horizontalCollision;
		mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false, collision));
		mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, collision));
	}
}
