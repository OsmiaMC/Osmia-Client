package dev.osmia.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class OsmiaTheme {
	public static final int ACCENT_START = 0x248CFF;
	public static final int ACCENT_END = 0x00FFFF;

	private OsmiaTheme() {
	}

	public static MutableComponent gradient(String text) {
		MutableComponent result = Component.empty();
		int lastCharacter = Math.max(1, text.length() - 1);

		for (int index = 0; index < text.length(); index++) {
			float progress = (float) index / lastCharacter;
			int red = interpolate(ACCENT_START >> 16 & 0xFF, ACCENT_END >> 16 & 0xFF, progress);
			int green = interpolate(ACCENT_START >> 8 & 0xFF, ACCENT_END >> 8 & 0xFF, progress);
			int blue = interpolate(ACCENT_START & 0xFF, ACCENT_END & 0xFF, progress);
			int color = red << 16 | green << 8 | blue;

			result.append(Component.literal(String.valueOf(text.charAt(index)))
					.withStyle(Style.EMPTY.withColor(color)));
		}

		return result;
	}

	public static int withAlpha(int rgb, int alpha) {
		return (Math.clamp(alpha, 0, 255) << 24) | (rgb & 0x00FFFFFF);
	}

	private static int interpolate(int start, int end, float progress) {
		return Math.round(start + (end - start) * progress);
	}
}
