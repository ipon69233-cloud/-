package dev.nova.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.nova.mixin.DrawContextAccessor;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

/**
 * Anti-aliased vector shapes for the GUI: rounded rectangles, circles and strokes.
 * Edges are smoothed with a one-pixel alpha feather, so no textures or shaders are needed.
 * All coordinates are in the current pose space of the DrawContext.
 */
public final class Shapes {
	private static final float MITER_LIMIT = 2.5f;

	private Shapes() {
	}

	public static void rect(DrawContext context, float x, float y, float w, float h, float radius, int color) {
		rect(context, x, y, w, h, radius, radius, radius, radius, color, color, false);
	}

	/** Left-to-right gradient. */
	public static void gradient(DrawContext context, float x, float y, float w, float h, float radius, int left, int right) {
		rect(context, x, y, w, h, radius, radius, radius, radius, left, right, false);
	}

	/** Top-to-bottom gradient. */
	public static void gradientDown(DrawContext context, float x, float y, float w, float h, float radius, int top, int bottom) {
		rect(context, x, y, w, h, radius, radius, radius, radius, top, bottom, true);
	}

	/** Rounded rectangle with individual corner radii, clockwise from the top left. */
	public static void rect(
		DrawContext context, float x, float y, float w, float h,
		float topLeft, float topRight, float bottomRight, float bottomLeft, int from, int to, boolean vertical
	) {
		if (w <= 0 || h <= 0 || (from | to) >>> 24 == 0) return;
		Mesh mesh = new Mesh(context);
		float[] points = roundedPath(mesh, x, y, w, h, topLeft, topRight, bottomRight, bottomLeft);
		mesh.setGradient(vertical ? y : x, vertical ? h : w, from, to, vertical);
		mesh.fill(points);
		mesh.submit();
	}

	public static void outline(DrawContext context, float x, float y, float w, float h, float radius, float width, int color) {
		outline(context, x, y, w, h, radius, radius, radius, radius, width, color);
	}

	/** Border drawn inside the given rectangle. */
	public static void outline(
		DrawContext context, float x, float y, float w, float h,
		float topLeft, float topRight, float bottomRight, float bottomLeft, float width, int color
	) {
		if (w <= 0 || h <= 0 || color >>> 24 == 0) return;
		Mesh mesh = new Mesh(context);
		float inset = width / 2f;
		float[] points = roundedPath(
			mesh, x + inset, y + inset, w - width, h - width,
			Math.max(0, topLeft - inset), Math.max(0, topRight - inset), Math.max(0, bottomRight - inset), Math.max(0, bottomLeft - inset)
		);
		mesh.setColor(color);
		mesh.stroke(points, true, width);
		mesh.submit();
	}

	public static void circle(DrawContext context, float cx, float cy, float radius, int color) {
		rect(context, cx - radius, cy - radius, radius * 2, radius * 2, radius, color);
	}

	public static void ring(DrawContext context, float cx, float cy, float radius, float width, int color) {
		outline(context, cx - radius - width / 2f, cy - radius - width / 2f, radius * 2 + width, radius * 2 + width, radius + width / 2f, width, color);
	}

	public static void line(DrawContext context, float x1, float y1, float x2, float y2, float width, int color) {
		polyline(context, new float[] {x1, y1, x2, y2}, false, width, color);
	}

	/** Stroke through the given x,y pairs. */
	public static void polyline(DrawContext context, float[] points, boolean closed, float width, int color) {
		if (color >>> 24 == 0) return;
		Mesh mesh = new Mesh(context);
		mesh.setColor(color);
		mesh.stroke(points, closed, width);
		mesh.submit();
	}

	/** Filled convex polygon through the given x,y pairs. */
	public static void polygon(DrawContext context, float[] points, int color) {
		if (color >>> 24 == 0) return;
		Mesh mesh = new Mesh(context);
		mesh.setColor(color);
		mesh.fill(points);
		mesh.submit();
	}

	private static float[] roundedPath(Mesh mesh, float x, float y, float w, float h, float tl, float tr, float br, float bl) {
		float max = Math.min(w, h) / 2f;
		float[] radii = {Math.min(tl, max), Math.min(tr, max), Math.min(br, max), Math.min(bl, max)};
		// Corner arc centers, clockwise from the top left, and the angle each arc starts at.
		float[] centersX = {x + radii[0], x + w - radii[1], x + w - radii[2], x + radii[3]};
		float[] centersY = {y + radii[0], y + radii[1], y + h - radii[2], y + h - radii[3]};
		FloatArrayList points = new FloatArrayList();
		for (int corner = 0; corner < 4; corner++) {
			float radius = radii[corner];
			if (radius < 0.01f) {
				points.add(centersX[corner]);
				points.add(centersY[corner]);
				continue;
			}
			int segments = Math.clamp((int) Math.ceil(radius * mesh.pixelScale * Math.PI / 2 / 2.5), 2, 24);
			double start = Math.PI + corner * Math.PI / 2;
			for (int i = 0; i <= segments; i++) {
				double angle = start + Math.PI / 2 * i / segments;
				points.add(centersX[corner] + (float) Math.cos(angle) * radius);
				points.add(centersY[corner] + (float) Math.sin(angle) * radius);
			}
		}
		return points.toFloatArray();
	}

	/** One batch of colored quads, submitted to the GUI renderer as a single element. */
	private static final class Mesh implements SimpleGuiElementRenderState {
		private final DrawContext context;
		private final Matrix3x2f pose;
		private final ScreenRect scissor;
		private final FloatArrayList vertices = new FloatArrayList();
		private final IntArrayList colors = new IntArrayList();
		/** Physical pixels per pose unit. */
		final float pixelScale;
		/** Width of the anti-aliasing feather in pose units, one physical pixel. */
		private final float feather;

		private float gradientStart;
		private float gradientLength;
		private int colorFrom;
		private int colorTo;
		private boolean gradientVertical;
		private float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

		Mesh(DrawContext context) {
			this.context = context;
			this.pose = new Matrix3x2f(context.getMatrices());
			this.scissor = context.scissorStack.peekLast();
			float poseScale = (float) Math.sqrt(pose.m00() * pose.m00() + pose.m01() * pose.m01());
			this.pixelScale = Math.max(0.01f, poseScale * MinecraftClient.getInstance().getWindow().getScaleFactor());
			this.feather = 1f / pixelScale;
		}

		void setColor(int color) {
			setGradient(0, 0, color, color, false);
		}

		void setGradient(float start, float length, int from, int to, boolean vertical) {
			gradientStart = start;
			gradientLength = length;
			colorFrom = from;
			colorTo = to;
			gradientVertical = vertical;
		}

		private int colorAt(float x, float y, float alpha) {
			int color = colorFrom;
			if (colorFrom != colorTo && gradientLength > 0) {
				float t = Math.clamp(((gradientVertical ? y : x) - gradientStart) / gradientLength, 0f, 1f);
				color = mix(colorFrom, colorTo, t);
			}
			if (alpha >= 1f) return color;
			return color & 0xFFFFFF | Math.round((color >>> 24) * alpha) << 24;
		}

		/** Per-vertex outward normals of a path, scaled so offsets keep parallel edges parallel. */
		private float[] normals(float[] points, boolean closed) {
			int count = points.length / 2;
			float area = 0;
			for (int i = 0; i < count; i++) {
				int next = (i + 1) % count;
				area += points[i * 2] * points[next * 2 + 1] - points[next * 2] * points[i * 2 + 1];
			}
			// Positive shoelace area means clockwise on screen (y grows downward).
			float side = area >= 0 ? 1f : -1f;
			float[] normals = new float[points.length];
			Vector2f before = new Vector2f();
			Vector2f after = new Vector2f();
			for (int i = 0; i < count; i++) {
				boolean hasBefore = closed || i > 0;
				boolean hasAfter = closed || i < count - 1;
				int prev = (i + count - 1) % count;
				int next = (i + 1) % count;
				if (hasBefore) edgeNormal(points, prev, i, side, before);
				if (hasAfter) edgeNormal(points, i, next, side, after);
				if (!hasBefore) before.set(after);
				if (!hasAfter) after.set(before);

				float nx = before.x + after.x;
				float ny = before.y + after.y;
				float length = (float) Math.sqrt(nx * nx + ny * ny);
				if (length < 1.0e-4f) {
					nx = after.x;
					ny = after.y;
				} else {
					nx /= length;
					ny /= length;
					float miter = Math.min(MITER_LIMIT, 1f / Math.max(0.2f, nx * after.x + ny * after.y));
					nx *= miter;
					ny *= miter;
				}
				normals[i * 2] = nx;
				normals[i * 2 + 1] = ny;
			}
			return normals;
		}

		private static void edgeNormal(float[] points, int from, int to, float side, Vector2f out) {
			float dx = points[to * 2] - points[from * 2];
			float dy = points[to * 2 + 1] - points[from * 2 + 1];
			float length = (float) Math.sqrt(dx * dx + dy * dy);
			if (length < 1.0e-5f) out.set(0, 0);
			else out.set(dy / length * side, -dx / length * side);
		}

		void fill(float[] points) {
			int count = points.length / 2;
			if (count < 3) return;
			float[] normals = normals(points, true);
			float centerX = 0, centerY = 0;
			for (int i = 0; i < count; i++) {
				centerX += points[i * 2];
				centerY += points[i * 2 + 1];
			}
			centerX /= count;
			centerY /= count;

			float half = feather / 2f;
			for (int i = 0; i < count; i++) {
				int next = (i + 1) % count;
				float ax = points[i * 2], ay = points[i * 2 + 1], bx = points[next * 2], by = points[next * 2 + 1];
				float anx = normals[i * 2] * half, any = normals[i * 2 + 1] * half;
				float bnx = normals[next * 2] * half, bny = normals[next * 2 + 1] * half;
				// Solid fan triangle, then the feather strip fading to transparent.
				quad(centerX, centerY, 1, ax - anx, ay - any, 1, bx - bnx, by - bny, 1, bx - bnx, by - bny, 1);
				quad(ax - anx, ay - any, 1, bx - bnx, by - bny, 1, bx + bnx, by + bny, 0, ax + anx, ay + any, 0);
			}
		}

		void stroke(float[] points, boolean closed, float width) {
			int count = points.length / 2;
			if (count < 2) return;
			float[] normals = normals(points, closed);
			// Strokes thinner than a pixel keep one pixel of coverage and fade instead.
			float alpha = Math.min(1f, width / feather);
			float half = Math.max(width, feather) / 2f;
			float inner = Math.max(0, half - feather / 2f);
			float outer = half + feather / 2f;
			int segments = closed ? count : count - 1;
			for (int i = 0; i < segments; i++) {
				int next = (i + 1) % count;
				float ax = points[i * 2], ay = points[i * 2 + 1], bx = points[next * 2], by = points[next * 2 + 1];
				float anx = normals[i * 2], any = normals[i * 2 + 1], bnx = normals[next * 2], bny = normals[next * 2 + 1];
				quad(
					ax - anx * inner, ay - any * inner, alpha, bx - bnx * inner, by - bny * inner, alpha,
					bx + bnx * inner, by + bny * inner, alpha, ax + anx * inner, ay + any * inner, alpha
				);
				quad(
					ax + anx * inner, ay + any * inner, alpha, bx + bnx * inner, by + bny * inner, alpha,
					bx + bnx * outer, by + bny * outer, 0, ax + anx * outer, ay + any * outer, 0
				);
				quad(
					ax - anx * inner, ay - any * inner, alpha, bx - bnx * inner, by - bny * inner, alpha,
					bx - bnx * outer, by - bny * outer, 0, ax - anx * outer, ay - any * outer, 0
				);
			}
		}

		private void quad(float ax, float ay, float aa, float bx, float by, float ba, float cx, float cy, float ca, float dx, float dy, float da) {
			// Vanilla GUI quads have a negative shoelace area; keep the same winding for culling.
			float area = ax * by - bx * ay + bx * cy - cx * by + cx * dy - dx * cy + dx * ay - ax * dy;
			if (area > 0) {
				vertex(ax, ay, aa);
				vertex(dx, dy, da);
				vertex(cx, cy, ca);
				vertex(bx, by, ba);
			} else {
				vertex(ax, ay, aa);
				vertex(bx, by, ba);
				vertex(cx, cy, ca);
				vertex(dx, dy, da);
			}
		}

		private void vertex(float x, float y, float alpha) {
			vertices.add(x);
			vertices.add(y);
			colors.add(colorAt(x, y, alpha));
			minX = Math.min(minX, x);
			minY = Math.min(minY, y);
			maxX = Math.max(maxX, x);
			maxY = Math.max(maxY, y);
		}

		void submit() {
			if (!colors.isEmpty()) ((DrawContextAccessor) context).nova$getState().addSimpleElement(this);
		}

		@Override
		public void setupVertices(VertexConsumer consumer) {
			for (int i = 0; i < colors.size(); i++) {
				consumer.vertex(pose, vertices.getFloat(i * 2), vertices.getFloat(i * 2 + 1)).color(colors.getInt(i));
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
			return scissor;
		}

		@Override
		public ScreenRect bounds() {
			int left = (int) Math.floor(minX), top = (int) Math.floor(minY);
			int right = (int) Math.ceil(maxX), bottom = (int) Math.ceil(maxY);
			ScreenRect rect = new ScreenRect(left, top, right - left, bottom - top).transformEachVertex(pose);
			return scissor != null ? scissor.intersection(rect) : rect;
		}
	}

	public static int mix(int from, int to, float t) {
		int a = channel(from >>> 24, to >>> 24, t);
		int r = channel(from >> 16 & 0xFF, to >> 16 & 0xFF, t);
		int g = channel(from >> 8 & 0xFF, to >> 8 & 0xFF, t);
		int b = channel(from & 0xFF, to & 0xFF, t);
		return a << 24 | r << 16 | g << 8 | b;
	}

	private static int channel(int from, int to, float t) {
		return Math.round(from + (to - from) * t);
	}
}
