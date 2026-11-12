package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.MultiSetting;
import dev.nova.setting.NumberSetting;
import dev.nova.util.WorldOverlay;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class BlockESP extends Module {
	/** Blocks looked at per tick, a scan of a large radius is spread over several ticks. */
	private static final int SCAN_BUDGET = 40_000;
	private static final int MAX_BOXES = 400;
	private static final int PAUSE_TICKS = 20;

	private record Ore(String item, int color) {
	}

	private record Target(BlockPos pos, Block block, Box box, int color) {
	}

	private static final Map<Block, Ore> ORES = new IdentityHashMap<>();

	static {
		register("Алмазы", 0xFF33E0E0, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
		register("Древние обломки", 0xFFA0522D, Blocks.ANCIENT_DEBRIS);
		register("Изумруды", 0xFF33DD55, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE);
		register("Золото", 0xFFFFD733, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE);
		register("Железо", 0xFFD8AF93, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
		register("Редстоун", 0xFFFF3333, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
		register("Лазурит", 0xFF3355FF, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
		register("Уголь", 0xFF707070, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
		register("Медь", 0xFFFF8844, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
	}

	private static void register(String item, int color, Block... blocks) {
		for (Block block : blocks) ORES.put(block, new Ore(item, color));
	}

	private final MultiSetting blocks = multi("Блоки")
		.add("Алмазы", true).add("Древние обломки", true).add("Изумруды", true).add("Золото", false).add("Железо", false)
		.add("Редстоун", false).add("Лазурит", false).add("Уголь", false).add("Медь", false);
	private final NumberSetting radius = number("Радиус", 24, 8, 48, 4);

	private List<Target> results = List.of();
	private List<Target> found = new ArrayList<>();
	private boolean scanning;
	private int pause;
	private BlockPos center = BlockPos.ORIGIN;
	private int scanRadius;
	private int minX, maxX, minY, maxY, minZ, maxZ;
	private int x, y, z;

	public BlockESP() {
		super("BlockESP", "Подсветка руды и ценных блоков", Category.VISUALS);
	}

	@Override
	protected void onEnable() {
		scanning = false;
		pause = 0;
	}

	@Override
	protected void onDisable() {
		scanning = false;
		results = List.of();
		found = new ArrayList<>();
	}

	@Override
	public void onTick() {
		if (!scanning) {
			if (pause-- > 0) return;
			begin();
		}
		scan();
	}

	private void begin() {
		center = mc.player.getBlockPos();
		scanRadius = radius.getInt();
		minX = center.getX() - scanRadius;
		maxX = center.getX() + scanRadius;
		minZ = center.getZ() - scanRadius;
		maxZ = center.getZ() + scanRadius;
		minY = Math.max(center.getY() - scanRadius, mc.world.getBottomY());
		maxY = Math.min(center.getY() + scanRadius, mc.world.getBottomY() + mc.world.getHeight() - 1);
		x = minX;
		y = minY;
		z = minZ;
		found = new ArrayList<>();
		scanning = true;
	}

	private void scan() {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		double limit = (double) scanRadius * scanRadius;
		for (int budget = SCAN_BUDGET; budget > 0 && x <= maxX; budget--) {
			pos.set(x, y, z);
			Block block = mc.world.getBlockState(pos).getBlock();
			Ore ore = ORES.get(block);
			if (ore != null && blocks.is(ore.item) && center.getSquaredDistance(pos) <= limit) {
				found.add(new Target(pos.toImmutable(), block, new Box(pos), ore.color));
			}
			if (++y > maxY) {
				y = minY;
				if (++z > maxZ) {
					z = minZ;
					x++;
				}
			}
		}
		if (x <= maxX) return;

		found.sort(Comparator.comparingDouble(target -> center.getSquaredDistance(target.pos)));
		results = found.size() > MAX_BOXES ? new ArrayList<>(found.subList(0, MAX_BOXES)) : found;
		found = new ArrayList<>();
		scanning = false;
		pause = PAUSE_TICKS;
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		WorldOverlay.Lines lines = WorldOverlay.begin(context);
		for (Target target : results) {
			// The block may have been mined since the scan.
			if (mc.world.getBlockState(target.pos).getBlock() == target.block) lines.box(target.box, target.color, 1.5f);
		}
		lines.submit(context);
	}

	@Override
	public String getSuffix() {
		return Integer.toString(results.size());
	}
}
