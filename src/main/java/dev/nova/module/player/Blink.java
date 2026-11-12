package dev.nova.module.player;

import dev.nova.module.Category;
import dev.nova.module.Module;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayList;
import java.util.List;

/** Outgoing packets are intercepted in ClientCommonNetworkHandlerMixin. */
public class Blink extends Module {
	private final List<Packet<?>> queue = new ArrayList<>();
	private boolean flushing;

	public Blink() {
		super("Blink", "Задержка отправки пакетов", Category.PLAYER);
	}

	/** Returns true when the packet was queued and must not be sent now. */
	public boolean capture(Packet<?> packet) {
		if (flushing || !(packet instanceof PlayerMoveC2SPacket) || mc.player == null) return false;
		queue.add(packet);
		return true;
	}

	@Override
	protected void onDisable() {
		ClientPlayNetworkHandler handler = mc.getNetworkHandler();
		if (handler != null && inGame()) {
			flushing = true;
			try {
				for (Packet<?> packet : queue) handler.sendPacket(packet);
			} finally {
				flushing = false;
			}
		}
		queue.clear();
	}

	@Override
	public String getSuffix() {
		return Integer.toString(queue.size());
	}
}
