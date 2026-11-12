package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;

import java.util.Set;

/** Hidden blocks are skipped while chunks are meshed, see BlockRenderManagerMixin. */
public class Xray extends Module {
	private static final Set<Block> VISIBLE = Set.of(
		Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
		Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.RAW_IRON_BLOCK,
		Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.RAW_COPPER_BLOCK,
		Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.RAW_GOLD_BLOCK, Blocks.NETHER_GOLD_ORE,
		Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
		Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
		Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
		Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
		Blocks.NETHER_QUARTZ_ORE, Blocks.ANCIENT_DEBRIS,
		Blocks.SPAWNER, Blocks.TRIAL_SPAWNER, Blocks.VAULT,
		Blocks.END_PORTAL_FRAME, Blocks.END_PORTAL, Blocks.NETHER_PORTAL,
		Blocks.AMETHYST_CLUSTER, Blocks.BUDDING_AMETHYST,
		Blocks.BEDROCK
	);

	private final BoolSetting lava = bool("Lava", true);
	private final BoolSetting water = bool("Water", false);

	public Xray() {
		super("Xray", "Показывает руды сквозь блоки", Category.VISUALS);
	}

	public boolean isVisible(BlockState state) {
		return VISIBLE.contains(state.getBlock());
	}

	public boolean isVisible(FluidState state) {
		return state.isIn(FluidTags.LAVA) ? lava.get() : water.get();
	}

	@Override
	protected void onEnable() {
		reloadChunks();
	}

	@Override
	protected void onDisable() {
		reloadChunks();
	}

	private static void reloadChunks() {
		if (mc.worldRenderer != null && mc.world != null) mc.worldRenderer.reload();
	}
}
