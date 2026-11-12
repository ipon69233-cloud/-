package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.module.Modules;

/** The brightness is raised in LightmapTextureManagerMixin, the gamma option stays untouched. */
public class Fullbright extends Module {
	public static final float BRIGHT_GAMMA = 16f;

	public Fullbright() {
		super("Fullbright", "Максимальная яркость в темноте", Category.RENDER);
	}

	/** Xray needs full brightness as well, ores are enclosed by unlit blocks. */
	public static boolean isBright() {
		return Modules.FULLBRIGHT.isEnabled() || Modules.XRAY.isEnabled();
	}
}
