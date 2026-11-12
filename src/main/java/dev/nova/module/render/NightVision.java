package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.module.Modules;

/** The brightness is raised in LightmapTextureManagerMixin, the gamma option stays untouched. */
public class NightVision extends Module {
	public static final float BRIGHT_GAMMA = 16f;

	public NightVision() {
		super("NightVision", "Ночное зрение под водой и в темноте", Category.VISUALS);
	}

	/** Xray needs full brightness as well, ores are enclosed by unlit blocks. */
	public static boolean isBright() {
		return Modules.NIGHT_VISION.isEnabled() || Modules.XRAY.isEnabled();
	}
}
