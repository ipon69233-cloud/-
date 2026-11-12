package dev.nova.gui.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Smooth text in the bundled Inter font. Glyph atlases are sampled without filtering, so each
 * weight is registered at several resolutions and the one closest to the on-screen size is used.
 */
public final class Fonts {
	public enum Weight {
		MEDIUM("medium"),
		SEMIBOLD("semibold");

		private final Style[] styles;

		Weight(String name) {
			int[] sizes = resolutions();
			styles = new Style[sizes.length];
			for (int i = 0; i < sizes.length; i++) {
				Identifier id = Identifier.of("nova", name + "_" + sizes[i]);
				styles[i] = Style.EMPTY.withFont(new StyleSpriteSource.Font(id));
			}
		}
	}

	/** Font size the providers are declared with, in GUI units. */
	private static final float BASE_SIZE = 10f;
	/** Glyph pixel heights of the registered variants; matches the files in assets/nova/font. */
	private static final int[] RESOLUTIONS = resolutions();
	/** Inter's ascent minus the 0.7 em the game places between the text origin and the baseline. */
	private static final float TOP_OFFSET = 0.27f;

	private Fonts() {
	}

	private static int[] resolutions() {
		int[] sizes = new int[37];
		for (int i = 0; i < 33; i++) sizes[i] = 8 + i;
		sizes[33] = 44;
		sizes[34] = 48;
		sizes[35] = 56;
		sizes[36] = 64;
		return sizes;
	}

	private static int nearest(float pixelHeight) {
		int best = 0;
		for (int i = 1; i < RESOLUTIONS.length; i++) {
			if (Math.abs(RESOLUTIONS[i] - pixelHeight) < Math.abs(RESOLUTIONS[best] - pixelHeight)) best = i;
		}
		return best;
	}

	/** Draws text whose line box starts at y; size is the font size in pose units. */
	public static void draw(DrawContext context, String text, float x, float y, float size, int color, Weight weight) {
		if (text.isEmpty() || color >>> 24 == 0) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		Matrix3x2fStack matrices = context.getMatrices();
		float poseScale = (float) Math.sqrt(matrices.m00() * matrices.m00() + matrices.m01() * matrices.m01());
		float pixelsPerUnit = poseScale * mc.getWindow().getScaleFactor();
		int variant = nearest(size * pixelsPerUnit);
		// Draw at exactly one texel per screen pixel: unfiltered glyphs lose thin strokes otherwise.
		float scale = RESOLUTIONS[variant] / (BASE_SIZE * pixelsPerUnit);
		y += size * TOP_OFFSET;
		// Snap glyph rows and columns to the pixel grid for the same reason. Glyph bitmaps hang off the
		// baseline, which the game puts 0.7 em below the origin, so that is the line to align vertically.
		float pixelX = (matrices.m00() * x + matrices.m20()) * mc.getWindow().getScaleFactor();
		float pixelY = (matrices.m11() * y + matrices.m21()) * mc.getWindow().getScaleFactor() + 0.7f * RESOLUTIONS[variant];
		x += (Math.round(pixelX) - pixelX) / pixelsPerUnit;
		y += (Math.round(pixelY) - pixelY) / pixelsPerUnit;

		matrices.pushMatrix();
		matrices.translate(x, y);
		matrices.scale(scale, scale);
		context.drawText(mc.textRenderer, Text.literal(text).setStyle(weight.styles[variant]), 0, 0, color, false);
		matrices.popMatrix();
	}

	public static void drawCentered(DrawContext context, String text, float centerX, float y, float size, int color, Weight weight) {
		draw(context, text, centerX - width(text, size, weight) / 2f, y, size, color, weight);
	}

	public static void drawRight(DrawContext context, String text, float right, float y, float size, int color, Weight weight) {
		draw(context, text, right - width(text, size, weight), y, size, color, weight);
	}

	public static float width(String text, float size, Weight weight) {
		if (text.isEmpty()) return 0;
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		// Advances are resolution independent, so any variant measures the same.
		Text styled = Text.literal(text).setStyle(weight.styles[nearest(32)]);
		return renderer.getTextHandler().getWidth(styled) * size / BASE_SIZE;
	}

	/** Greedy word wrap into at most maxLines lines; the last line is cut with an ellipsis. */
	public static List<String> wrap(String text, float maxWidth, float size, Weight weight, int maxLines) {
		List<String> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (String word : text.split(" ")) {
			String candidate = line.isEmpty() ? word : line + " " + word;
			if (line.isEmpty() || width(candidate, size, weight) <= maxWidth) {
				line.setLength(0);
				line.append(candidate);
				continue;
			}
			lines.add(line.toString());
			line.setLength(0);
			line.append(word);
			if (lines.size() == maxLines) break;
		}
		if (lines.size() < maxLines && !line.isEmpty()) {
			lines.add(line.toString());
		} else if (!line.isEmpty()) {
			lines.set(maxLines - 1, trim(lines.get(maxLines - 1) + "…", maxWidth, size, weight));
		}
		return lines;
	}

	/** Cuts text to fit maxWidth, ending in an ellipsis when shortened. */
	public static String trim(String text, float maxWidth, float size, Weight weight) {
		if (width(text, size, weight) <= maxWidth) return text;
		String cut = text.endsWith("…") ? text.substring(0, text.length() - 1) : text;
		while (!cut.isEmpty() && width(cut + "…", size, weight) > maxWidth) cut = cut.substring(0, cut.length() - 1);
		return cut.stripTrailing() + "…";
	}
}
