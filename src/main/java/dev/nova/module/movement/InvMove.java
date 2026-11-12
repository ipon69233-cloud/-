package dev.nova.module.movement;

import dev.nova.gui.ClickGuiScreen;
import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.util.KeyUtil;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.screen.ingame.JigsawBlockScreen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;

public class InvMove extends Module {
	private boolean active;

	public InvMove() {
		super("InvMove", "Движение в открытом инвентаре", Category.MOVEMENT);
	}

	private static KeyBinding[] keys() {
		return new KeyBinding[] {
			mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey,
			mc.options.jumpKey, mc.options.sprintKey, mc.options.sneakKey
		};
	}

	@Override
	public void onTick() {
		Screen screen = mc.currentScreen;
		if (screen == null) {
			active = false;
			return;
		}
		if (!isEligible(screen)) {
			if (active) {
				for (KeyBinding key : keys()) key.setPressed(false);
				active = false;
			}
			return;
		}
		// Opening a screen unpresses every key, so the real state is applied again each tick.
		active = true;
		for (KeyBinding key : keys()) key.setPressed(KeyUtil.isDown(key));
	}

	@Override
	protected void onDisable() {
		if (!active) return;
		active = false;
		for (KeyBinding key : keys()) {
			if (mc.currentScreen == null) KeyUtil.release(key);
			else key.setPressed(false);
		}
	}

	private static boolean isEligible(Screen screen) {
		if (screen instanceof ClickGuiScreen gui && gui.isTyping()) return false;
		if (screen instanceof ChatScreen || screen instanceof AbstractSignEditScreen || screen instanceof BookEditScreen
			|| screen instanceof AbstractCommandBlockScreen || screen instanceof AnvilScreen
			|| screen instanceof StructureBlockScreen || screen instanceof JigsawBlockScreen) return false;

		// Creative search, recipe book search and other focused text fields.
		Element focused = screen;
		while (focused instanceof ParentElement parent && parent.getFocused() != null) focused = parent.getFocused();
		return !(focused instanceof TextFieldWidget field && field.isFocused());
	}
}
