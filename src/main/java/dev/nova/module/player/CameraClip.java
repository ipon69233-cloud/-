package dev.nova.module.player;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.NumberSetting;

/** The distance is applied in CameraMixin, blocks no longer pull the camera closer. */
public class CameraClip extends Module {
	private final NumberSetting distance = number("Дистанция", 4, 1, 10, 0.5);

	public CameraClip() {
		super("CameraClip", "Камера проходит сквозь блоки", Category.PLAYER);
	}

	public float getDistance() {
		return distance.getFloat();
	}

	@Override
	public String getSuffix() {
		return distance.format();
	}
}
