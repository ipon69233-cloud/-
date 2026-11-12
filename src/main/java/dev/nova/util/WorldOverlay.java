package dev.nova.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.nova.mixin.DrawContextAccessor;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Draws world-space lines on the HUD layer by projecting them with the matrices of the
 * current frame. This keeps world overlays independent of the world render pipeline.
 */
public final class WorldOverlay {
	private static final float NEAR = 0.1f;
	private static final Matrix3x2f IDENTITY = new Matrix3x2f();
	private static final Vector3f POINT = new Vector3f();
	private static final Vector4f CLIP = new Vector4f();

	private static final Matrix4f view = new Matrix4f();
	private static final Matrix4f projection = new Matrix4f();
	private static Vec3d cameraPos = Vec3d.ZERO;

	private WorldOverlay() {
	}

	/** Called at the start of world rendering with the matrices used for this frame. */
	public static void capture(Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
		view.set(positionMatrix);
		projection.set(projectionMatrix);
		cameraPos = camera.getCameraPos();
	}

	public static Lines begin(DrawContext context) {
		return new Lines(context.getScaledWindowWidth(), context.getScaledWindowHeight());
	}

	/** Projects a world position to scaled GUI coordinates. Returns false when it is behind the camera. */
	public static boolean project(Vec3d world, float[] out) {
		Vector3f point = POINT.set((float) (world.x - cameraPos.x), (float) (world.y - cameraPos.y), (float) (world.z - cameraPos.z));
		view.transformPosition(point);
		if (point.z > -NEAR) return false;
		projection.transform(CLIP.set(point.x, point.y, point.z, 1f));
		Window window = MinecraftClient.getInstance().getWindow();
		out[0] = (CLIP.x / CLIP.w * 0.5f + 0.5f) * window.getScaledWidth();
		out[1] = (0.5f - CLIP.y / CLIP.w * 0.5f) * window.getScaledHeight();
		return true;
	}

	/** One batch of line quads, submitted to the GUI renderer as a single element. */
	public static final class Lines implements SimpleGuiElementRenderState {
		private final FloatArrayList vertices = new FloatArrayList();
		private final IntArrayList colors = new IntArrayList();
		private final int width;
		private final int height;
		private final float pixel;
		private final Vector3f a = new Vector3f();
		private final Vector3f b = new Vector3f();
		private final Vector4f clip = new Vector4f();
		private final float[] screen = new float[4];

		private Lines(int width, int height) {
			this.width = width;
			this.height = height;
			this.pixel = 1f / MinecraftClient.getInstance().getWindow().getScaleFactor();
		}

		private void toCamera(Vec3d world, Vector3f out) {
			out.set((float) (world.x - cameraPos.x), (float) (world.y - cameraPos.y), (float) (world.z - cameraPos.z));
			view.transformPosition(out);
		}

		private void toScreen(Vector3f camera, int offset) {
			projection.transform(clip.set(camera.x, camera.y, camera.z, 1f));
			screen[offset] = (clip.x / clip.w * 0.5f + 0.5f) * width;
			screen[offset + 1] = (0.5f - clip.y / clip.w * 0.5f) * height;
		}

		/** Clips the camera-space segment a-b against the near plane and projects it. */
		private boolean project() {
			if (a.z > -NEAR && b.z > -NEAR) return false;
			if (a.z > -NEAR) a.lerp(b, (-NEAR - a.z) / (b.z - a.z));
			else if (b.z > -NEAR) b.lerp(a, (-NEAR - b.z) / (a.z - b.z));
			toScreen(a, 0);
			toScreen(b, 2);
			return true;
		}

		public void line(Vec3d from, Vec3d to, int color, float thickness) {
			toCamera(from, a);
			toCamera(to, b);
			if (project()) line2d(screen[0], screen[1], screen[2], screen[3], color, thickness);
		}

		/** Line from the crosshair to a world position. */
		public void tracer(Vec3d target, int color, float thickness) {
			a.set(0f, 0f, -NEAR * 2f);
			toCamera(target, b);
			if (project()) line2d(width / 2f, height / 2f, screen[2], screen[3], color, thickness);
		}

		public void box(Box box, int color, float thickness) {
			Vec3d[] c = {
				new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.minZ),
				new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.minX, box.minY, box.maxZ),
				new Vec3d(box.minX, box.maxY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ),
				new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ)
			};
			for (int i = 0; i < 4; i++) {
				int next = (i + 1) % 4;
				line(c[i], c[next], color, thickness);
				line(c[i + 4], c[next + 4], color, thickness);
				line(c[i], c[i + 4], color, thickness);
			}
		}

		/** Horizontal circle around the center, in the plane of its y coordinate. */
		public void circle(Vec3d center, double radius, int segments, int color, float thickness) {
			Vec3d previous = null;
			for (int i = 0; i <= segments; i++) {
				double angle = Math.PI * 2 * i / segments;
				Vec3d point = new Vec3d(center.x + Math.cos(angle) * radius, center.y, center.z + Math.sin(angle) * radius);
				if (previous != null) line(previous, point, color, thickness);
				previous = point;
			}
		}

		/** Filled triangle in scaled GUI coordinates. */
		public void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int color) {
			// Match the winding of vanilla GUI quads, the pipeline culls the other side.
			if ((x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1) > 0) {
				float tx = x2;
				float ty = y2;
				x2 = x3;
				y2 = y3;
				x3 = tx;
				y3 = ty;
			}
			// A quad with a repeated vertex renders as a single triangle.
			vertices.add(x1); vertices.add(y1);
			vertices.add(x2); vertices.add(y2);
			vertices.add(x3); vertices.add(y3);
			vertices.add(x3); vertices.add(y3);
			colors.add(color);
		}

		/** Thickness is in physical pixels. */
		private void line2d(float x1, float y1, float x2, float y2, int color, float thickness) {
			float dx = x2 - x1;
			float dy = y2 - y1;
			float length = (float) Math.sqrt(dx * dx + dy * dy);
			if (length < 1.0e-3f || Float.isNaN(length)) return;
			float half = thickness * pixel * 0.5f;
			float nx = -dy / length * half;
			float ny = dx / length * half;
			// Same winding as vanilla GUI quads.
			vertices.add(x1 - nx); vertices.add(y1 - ny);
			vertices.add(x1 + nx); vertices.add(y1 + ny);
			vertices.add(x2 + nx); vertices.add(y2 + ny);
			vertices.add(x2 - nx); vertices.add(y2 - ny);
			colors.add(color);
		}

		public void submit(DrawContext context) {
			if (!colors.isEmpty()) ((DrawContextAccessor) context).nova$getState().addSimpleElement(this);
		}

		@Override
		public void setupVertices(VertexConsumer consumer) {
			for (int quad = 0; quad < colors.size(); quad++) {
				int color = colors.getInt(quad);
				for (int v = 0; v < 4; v++) {
					int i = quad * 8 + v * 2;
					consumer.vertex(IDENTITY, vertices.getFloat(i), vertices.getFloat(i + 1)).color(color);
				}
			}
		}

		@Override
		public RenderPipeline pipeline() {
			return RenderPipelines.GUI;
		}

		@Override
		public TextureSetup textureSetup() {
			return TextureSetup.empty();
		}

		@Override
		public ScreenRect scissorArea() {
			return null;
		}

		@Override
		public ScreenRect bounds() {
			return new ScreenRect(0, 0, width, height);
		}
	}
}
