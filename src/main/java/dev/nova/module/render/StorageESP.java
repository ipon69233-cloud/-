package dev.nova.module.render;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.BoolSetting;
import dev.nova.util.WorldOverlay;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class StorageESP extends Module {
	private static final int REFRESH_TICKS = 10;

	private final BoolSetting chests = bool("Сундуки", true);
	private final BoolSetting enderChests = bool("Эндер-сундуки", true);
	private final BoolSetting shulkers = bool("Шалкеры", true);
	private final BoolSetting spawners = bool("Спавнеры", true);
	private final BoolSetting other = bool("Другие", false);

	private record Target(Box box, int color) {
	}

	private List<Target> targets = List.of();
	private int timer;

	public StorageESP() {
		super("StorageESP", "Подсвечивает сундуки, шалкеры и спавнеры", Category.VISUALS);
	}

	@Override
	protected void onEnable() {
		timer = 0;
	}

	@Override
	public void onTick() {
		if (timer-- > 0) return;
		timer = REFRESH_TICKS;

		List<Target> found = new ArrayList<>();
		ChunkPos center = mc.player.getChunkPos();
		int radius = mc.options.getClampedViewDistance();
		for (int x = center.x - radius; x <= center.x + radius; x++) {
			for (int z = center.z - radius; z <= center.z + radius; z++) {
				WorldChunk chunk = mc.world.getChunkManager().getChunk(x, z, ChunkStatus.FULL, false);
				if (chunk == null) continue;
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					int color = colorOf(blockEntity);
					if (color != 0) found.add(new Target(boxOf(blockEntity.getPos()), color));
				}
			}
		}
		targets = found;
	}

	private int colorOf(BlockEntity blockEntity) {
		if (blockEntity instanceof ShulkerBoxBlockEntity) return shulkers.get() ? 0xFFFF55FF : 0;
		if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof BarrelBlockEntity) return chests.get() ? 0xFFFFAA00 : 0;
		if (blockEntity instanceof EnderChestBlockEntity) return enderChests.get() ? 0xFFAA55FF : 0;
		if (blockEntity instanceof MobSpawnerBlockEntity) return spawners.get() ? 0xFFFF5555 : 0;
		if (blockEntity instanceof LockableContainerBlockEntity) return other.get() ? 0xFFAAAAAA : 0;
		return 0;
	}

	private static Box boxOf(BlockPos pos) {
		BlockState state = mc.world.getBlockState(pos);
		VoxelShape shape = state.getOutlineShape(mc.world, pos);
		return (shape.isEmpty() ? new Box(0, 0, 0, 1, 1, 1) : shape.getBoundingBox()).offset(pos);
	}

	@Override
	public void onRender2D(DrawContext context, float tickDelta) {
		WorldOverlay.Lines lines = WorldOverlay.begin(context);
		for (Target target : targets) lines.box(target.box, target.color, 1.5f);
		lines.submit(context);
	}

	@Override
	public String getSuffix() {
		return Integer.toString(targets.size());
	}
}
