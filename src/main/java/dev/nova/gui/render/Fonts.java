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

		private final Style[] styles = new Style[RESOLUTIONS.length];

		Weight(String name) {
			for (int i = 0; i < RESOLUTIONS.length; i++) {
				Identifier id = Identifier.of("nova", name + "_" + RESOLUTIONS[i]);
				styles[i] = Style.EMPTY.withFont(new StyleSpriteSource.Font(id));
			}
		}
	}

	/** Font size the providers are declared with, in GUI units. */
	private static final float BASE_SIZE = 10f;
	/** Glyph pixel heights of the registered variants; matches the files in assets/nova/font. */
	private static final int[] RESOLUTIONS = {10, 13, 16, 20, 25, 30, 40, 50, 60};
	/** Distance from the top of a line box to the vanilla text origin, as a fraction of the size. */
	private static final float TOP_OFFSET = 0.02f;

	private Fonts() {
	}

	private static Style style(Weight weight, float pixelHeight) {
		int best = 0;
		for (int i = 1; i < RESOLUTIONS.length; i++) {
			if (Math.abs(RESOLUTIONS[i] - pixelHeight) < Math.abs(RESOLUTIONS[best] - pixelHeight)) best = i;
		}
		return weight.styles[best];
	}

	/** Draws text whose line box starts at y; size is the font size in pose units. */
	public static void draw(DrawContext context, String text, float x, float y, float size, int color, Weight weight) {
		if (text.isEmpty() || color >>> 24 == 0) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		Matrix3x2fStack matrices = context.getMatrices();
		float poseScale = (float) Math.sqrt(matrices.m00() * matrices.m00() + matrices.m01() * matrices.m01());
		float pixelHeight = size * poseScale * mc.getWindow().getScaleFactor();
		float scale = size / BASE_SIZE;

		matrices.pushMatrix();
		matrices.translate(x, y + size * TOP_OFFSET);
		matrices.scale(scale, scale);
		context.drawText(mc.textRenderer, Text.literal(text).setStyle(style(weight, pixelHeight)), 0, 0, color, false);
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
		Text styled = Text.literal(text).setStyle(weight.styles[RESOLUTIONS.length / 2]);
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
