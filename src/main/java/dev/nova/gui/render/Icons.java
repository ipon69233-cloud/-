package dev.nova.gui.render;

import dev.nova.module.Category;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;

/** Line icons drawn on a 24x24 grid and scaled to the requested size. */
public final class Icons {
	private static final float STROKE = 1.8f;

	private Icons() {
	}

	private static void begin(DrawContext context, float x, float y, float size) {
		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.translate(x, y);
		matrices.scale(size / 24f, size / 24f);
	}

	private static void end(DrawContext context) {
		context.getMatrices().popMatrix();
	}

	private static void path(DrawContext context, int color, float... points) {
		Shapes.polyline(context, points, false, STROKE, color);
	}

	/** Samples a cubic Bézier curve; the result excludes the start point. */
	private static void cubic(float[] out, int offset, int steps, float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
		for (int i = 1; i <= steps; i++) {
			float t = i / (float) steps;
			float u = 1 - t;
			out[offset + (i - 1) * 2] = u * u * u * x0 + 3 * u * u * t * x1 + 3 * u * t * t * x2 + t * t * t * x3;
			out[offset + (i - 1) * 2 + 1] = u * u * u * y0 + 3 * u * u * t * y1 + 3 * u * t * t * y2 + t * t * t * y3;
		}
	}

	public static void category(DrawContext context, Category category, float x, float y, float size, int color) {
		begin(context, x, y, size);
		switch (category) {
			case COMBAT -> {
				path(context, color, 14.5f, 17.5f, 3, 6, 3, 3, 6, 3, 17.5f, 14.5f);
				Shapes.polyline(context, new float[] {13, 19, 19, 13, 21, 15, 15, 21}, true, STROKE, color);
				path(context, color, 16, 16, 20, 20);
			}
			case MOVEMENT -> {
				path(context, color, 13, 4, 13, 20);
				path(context, color, 13, 8, 18, 5);
				path(context, color, 13, 13, 19, 15);
				Shapes.ring(context, 9, 5, 2, STROKE, color);
				path(context, color, 7, 20, 9, 13, 6, 11);
			}
			case VISUALS -> eye(context, color);
			case PLAYER -> user(context, color);
			case MISC -> {
				for (int i = 0; i < 4; i++) {
					Shapes.outline(context, i % 2 == 0 ? 3 : 14, i < 2 ? 3 : 14, 7, 7, 1.5f, STROKE, color);
				}
			}
		}
		end(context);
	}

	private static void eye(DrawContext context, int color) {
		int steps = 8;
		float[] outline = new float[steps * 2 * 4];
		cubic(outline, 0, steps, 2, 12, 2, 12, 6, 5, 12, 5);
		cubic(outline, steps * 2, steps, 12, 5, 18, 5, 22, 12, 22, 12);
		cubic(outline, steps * 4, steps, 22, 12, 22, 12, 18, 19, 12, 19);
		cubic(outline, steps * 6, steps, 12, 19, 6, 19, 2, 12, 2, 12);
		Shapes.polyline(context, outline, true, STROKE, color);
		Shapes.ring(context, 12, 12, 3, STROKE, color);
	}

	private static void user(DrawContext context, int color) {
		Shapes.ring(context, 12, 8, 4, STROKE, color);
		int steps = 8;
		float[] shoulders = new float[2 + steps * 4];
		shoulders[0] = 4;
		shoulders[1] = 20;
		cubic(shoulders, 2, steps, 4, 20, 4, 16, 8, 14, 12, 14);
		cubic(shoulders, 2 + steps * 2, steps, 12, 14, 16, 14, 20, 16, 20, 20);
		Shapes.polyline(context, shoulders, false, STROKE, color);
	}

	public static void user(DrawContext context, float x, float y, float size, int color) {
		begin(context, x, y, size);
		user(context, color);
		end(context);
	}

	public static void gear(DrawContext context, float x, float y, float size, int color) {
		begin(context, x, y, size);
		int teeth = 8;
		float[] cog = new float[teeth * 8];
		for (int i = 0; i < teeth; i++) {
			double step = Math.PI * 2 / teeth;
			double base = i * step;
			// Each tooth: rise, flat top, fall, then the valley to the next tooth.
			double[] angles = {base - step * 0.2, base - step * 0.12, base + step * 0.12, base + step * 0.2};
			float[] radii = {7.6f, 10f, 10f, 7.6f};
			for (int j = 0; j < 4; j++) {
				cog[i * 8 + j * 2] = 12 + (float) Math.cos(angles[j]) * radii[j];
				cog[i * 8 + j * 2 + 1] = 12 + (float) Math.sin(angles[j]) * radii[j];
			}
		}
		Shapes.polyline(context, cog, true, STROKE, color);
		Shapes.ring(context, 12, 12, 3, STROKE, color);
		end(context);
	}

	public static void keyboard(DrawContext context, float x, float y, float size, int color) {
		begin(context, x, y, size);
		Shapes.outline(context, 2, 6, 20, 12, 2, STROKE, color);
		for (int keyX = 6; keyX <= 18; keyX += 4) Shapes.circle(context, keyX, 10, 1f, color);
		path(context, color, 7, 14, 17, 14);
		end(context);
	}

	public static void search(DrawContext context, float x, float y, float size, int color) {
		begin(context, x, y, size);
		Shapes.ring(context, 11, 11, 7, 2.2f, color);
		Shapes.line(context, 16.2f, 16.2f, 21, 21, 2.2f, color);
		end(context);
	}

	public static void chevronDown(DrawContext context, float x, float y, float size, int color) {
		begin(context, x, y, size);
		Shapes.polyline(context, new float[] {6, 9, 12, 15, 18, 9}, false, 2.6f, color);
		end(context);
	}

	public static void chevronUp(DrawContext context, float x, float y, float size, int color) {
		begin(context, x, y, size);
		Shapes.polyline(context, new float[] {6, 15, 12, 9, 18, 15}, false, 2.6f, color);
		end(context);
	}

	public static void chevronRight(DrawContext context, float x, float y, float size, int color) {
		begin(context, x, y, size);
		Shapes.polyline(context, new float[] {9, 18, 15, 12, 9, 6}, false, 2.4f, color);
		end(context);
	}

	public static void close(DrawContext context, float x, float y, float size, int color) {
		begin(context, x, y, size);
		Shapes.line(context, 18, 6, 6, 18, 2.4f, color);
		Shapes.line(context, 6, 6, 18, 18, 2.4f, color);
		end(context);
	}

	/** The client's mark: a bold N. */
	public static void logo(DrawContext context, float x, float y, float size, int color) {
		begin(context, x, y, size);
		Shapes.polyline(context, new float[] {6, 19.5f, 6, 4.5f, 18, 19.5f, 18, 4.5f}, false, 3.4f, color);
		end(context);
	}
}
