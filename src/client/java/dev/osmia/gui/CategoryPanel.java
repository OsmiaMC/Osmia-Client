package dev.osmia.gui;

import dev.osmia.render.OsmiaRoundedRect;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

final class CategoryPanel {
	static final int WIDTH = 124;

	private static final int HEADER_HEIGHT = 22;
	private static final int ROW_HEIGHT = 18;
	private static final int BODY_PADDING = 4;
	private static final int PADDING = 6;
	private static final int ARROW_HIT_WIDTH = 16;
	private static final int SETTING_INDENT = 10;
	private static final int TOGGLE_SIZE = 8;
	private static final int PANEL_CORNER = 5;
	private static final int TOGGLE_CORNER = 2;

	private static final int HEADER_BACKGROUND = 0xF00A0A14;
	private static final int BODY_BACKGROUND = 0xD0050510;
	private static final int HEADER_HOVER_TINT = 0x18FFFFFF;
	private static final int ARROW_COLOR = 0xFFC8C8D4;
	private static final int ARROW_HOVER_COLOR = 0xFF00FFFF;
	private static final int TOGGLE_ENABLED = 0xFF00FFFF;
	private static final int TOGGLE_DISABLED = 0x55141420;
	private static final int TEXT_DISABLED = 0xFFB0B0BC;

	private final String title;
	private final int x;
	private final int y;
	private final List<ToggleRow> rows = new ArrayList<>();
	private boolean collapsed;

	CategoryPanel(String title, int x, int y) {
		this.title = title;
		this.x = x;
		this.y = y;
	}

	ToggleRow addToggle(String label, BooleanSupplier enabled, Runnable toggle) {
		ToggleRow row = new ToggleRow(label, enabled, toggle);
		rows.add(row);
		return row;
	}

	void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		int height = height();
		OsmiaRoundedRect.fill(
				graphics,
				x,
				y,
				x + WIDTH,
				y + height,
				PANEL_CORNER,
				BODY_BACKGROUND,
				OsmiaRoundedRect.ROUND_BOTH
		);
		fillHeader(graphics, HEADER_BACKGROUND);
		if (contains(mouseX, mouseY, x, y, WIDTH, HEADER_HEIGHT)) {
			fillHeader(graphics, HEADER_HOVER_TINT);
		}

		int textY = y + (HEADER_HEIGHT - font.lineHeight) / 2 + 1;
		graphics.text(font, OsmiaTheme.gradient(title), x + PADDING, textY, 0xFFFFFFFF);
		renderArrow(
				graphics,
				font,
				x + WIDTH - PADDING - font.width(">"),
				textY,
				!collapsed,
				contains(mouseX, mouseY, x + WIDTH - ARROW_HIT_WIDTH, y, ARROW_HIT_WIDTH, HEADER_HEIGHT)
		);

		if (!collapsed && !rows.isEmpty()) {
			renderRows(graphics, font, mouseX, mouseY);
		}
	}

	boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !contains(mouseX, mouseY, x, y, WIDTH, height())) {
			return false;
		}

		if (mouseY < y + HEADER_HEIGHT) {
			collapsed = !collapsed;
			return true;
		}

		if (collapsed) {
			return false;
		}

		int rowY = y + HEADER_HEIGHT + BODY_PADDING;
		for (ToggleRow row : rows) {
			if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
				if (row.hasSettings() && mouseX >= x + WIDTH - ARROW_HIT_WIDTH) {
					row.toggleSettings();
				} else {
					row.toggle();
				}
				return true;
			}
			rowY += ROW_HEIGHT;

			if (row.settingsExpanded()) {
				for (ToggleRow setting : row.settings()) {
					if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
						setting.toggle();
						return true;
					}
					rowY += ROW_HEIGHT;
				}
			}
		}

		return false;
	}

	private void renderRows(
			GuiGraphicsExtractor graphics,
			Font font,
			int mouseX,
			int mouseY
	) {
		int rowY = y + HEADER_HEIGHT + BODY_PADDING;
		for (ToggleRow row : rows) {
			renderToggle(graphics, font, row, rowY, x + 2);
			if (row.hasSettings()) {
				boolean hovered = contains(
						mouseX,
						mouseY,
						x + WIDTH - ARROW_HIT_WIDTH,
						rowY,
						ARROW_HIT_WIDTH,
						ROW_HEIGHT
				);
				int textY = rowY + (ROW_HEIGHT - font.lineHeight) / 2 + 1;
				renderArrow(
						graphics,
						font,
						x + WIDTH - PADDING - font.width(">"),
						textY,
						row.settingsExpanded(),
						hovered
				);
			}
			rowY += ROW_HEIGHT;

			if (row.settingsExpanded()) {
				for (ToggleRow setting : row.settings()) {
					renderToggle(graphics, font, setting, rowY, x + SETTING_INDENT);
					rowY += ROW_HEIGHT;
				}
			}
		}
	}

	private void renderToggle(
			GuiGraphicsExtractor graphics,
			Font font,
			ToggleRow row,
			int rowY,
			int rowLeft
	) {
		boolean enabled = row.enabled();
		int boxX = rowLeft + PADDING - 2;
		int boxY = rowY + (ROW_HEIGHT - TOGGLE_SIZE) / 2;
		OsmiaRoundedRect.fill(
				graphics,
				boxX,
				boxY,
				boxX + TOGGLE_SIZE,
				boxY + TOGGLE_SIZE,
				TOGGLE_CORNER,
				enabled ? TOGGLE_ENABLED : TOGGLE_DISABLED,
				OsmiaRoundedRect.ROUND_BOTH
		);

		int textX = boxX + TOGGLE_SIZE + PADDING;
		int textY = rowY + (ROW_HEIGHT - font.lineHeight) / 2 + 1;
		Component label = enabled ? OsmiaTheme.gradient(row.label()) : Component.literal(row.label());
		graphics.text(font, label, textX, textY, enabled ? 0xFFFFFFFF : TEXT_DISABLED);
	}

	private void renderArrow(
			GuiGraphicsExtractor graphics,
			Font font,
			int arrowX,
			int arrowY,
			boolean expanded,
			boolean hovered
	) {
		String arrow = ">";
		float centerX = arrowX + font.width(arrow) / 2.0F;
		float centerY = arrowY + font.lineHeight / 2.0F;

		graphics.pose().pushMatrix();
		graphics.pose().translate(centerX, centerY);
		if (expanded) {
			graphics.pose().rotate((float) (Math.PI / 2.0));
		}
		graphics.pose().translate(-centerX, -centerY);
		graphics.text(
				font,
				Component.literal(arrow),
				arrowX,
				arrowY,
				hovered || expanded ? ARROW_HOVER_COLOR : ARROW_COLOR
		);
		graphics.pose().popMatrix();
	}

	private void fillHeader(GuiGraphicsExtractor graphics, int color) {
		OsmiaRoundedRect.fill(
				graphics,
				x,
				y,
				x + WIDTH,
				y + HEADER_HEIGHT,
				PANEL_CORNER,
				color,
				OsmiaRoundedRect.ROUND_BOTH
		);
	}

	private int height() {
		if (collapsed || rows.isEmpty()) {
			return HEADER_HEIGHT;
		}

		int contentHeight = rows.stream().mapToInt(ToggleRow::renderedHeight).sum();
		return HEADER_HEIGHT + BODY_PADDING * 2 + contentHeight;
	}

	private static boolean contains(
			double pointX,
			double pointY,
			int left,
			int top,
			int width,
			int height
	) {
		return pointX >= left && pointX < left + width && pointY >= top && pointY < top + height;
	}

	static final class ToggleRow {
		private final String label;
		private final BooleanSupplier enabled;
		private final Runnable toggle;
		private final List<ToggleRow> settings = new ArrayList<>();
		private boolean settingsExpanded;

		private ToggleRow(String label, BooleanSupplier enabled, Runnable toggle) {
			this.label = label;
			this.enabled = enabled;
			this.toggle = toggle;
		}

		ToggleRow addSetting(String label, BooleanSupplier enabled, Runnable toggle) {
			settings.add(new ToggleRow(label, enabled, toggle));
			return this;
		}

		private String label() {
			return label;
		}

		private boolean enabled() {
			return enabled.getAsBoolean();
		}

		private void toggle() {
			toggle.run();
		}

		private boolean hasSettings() {
			return !settings.isEmpty();
		}

		private List<ToggleRow> settings() {
			return settings;
		}

		private boolean settingsExpanded() {
			return settingsExpanded && hasSettings();
		}

		private void toggleSettings() {
			settingsExpanded = !settingsExpanded;
		}

		private int renderedHeight() {
			return ROW_HEIGHT + (settingsExpanded() ? settings.size() * ROW_HEIGHT : 0);
		}
	}
}
