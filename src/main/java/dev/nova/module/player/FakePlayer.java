package dev.nova.module.player;

import com.mojang.authlib.GameProfile;
import dev.nova.module.Category;
import dev.nova.module.Module;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;

import java.util.UUID;

public class FakePlayer extends Module {
	// Negative so it can never clash with an id handed out by a server.
	private static final int ENTITY_ID = -4242;

	private OtherClientPlayerEntity fake;

	public FakePlayer() {
		super("FakePlayer", "Ставит куклу игрока для теста", Category.PLAYER);
	}

	@Override
	protected void onEnable() {
		if (inGame()) spawn();
	}

	@Override
	protected void onDisable() {
		remove();
	}

	@Override
	public void onTick() {
		// The world changes under an enabled module when the player rejoins.
		if (fake == null || fake.isRemoved() || fake.getEntityWorld() != mc.world) spawn();
	}

	private void spawn() {
		remove();
		fake = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), "FakePlayer"));
		fake.copyPositionAndRotation(mc.player);
		fake.setHeadYaw(mc.player.getHeadYaw());
		fake.setBodyYaw(mc.player.bodyYaw);
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			fake.equipStack(slot, mc.player.getEquippedStack(slot).copy());
		}
		fake.setId(ENTITY_ID);
		mc.world.addEntity(fake);
	}

	private void remove() {
		if (fake != null && mc.world != null) mc.world.removeEntity(fake.getId(), Entity.RemovalReason.DISCARDED);
		fake = null;
	}
}
