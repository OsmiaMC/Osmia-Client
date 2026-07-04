package dev.osmia.gui;

import dev.osmia.OsmiaClient;
import dev.osmia.config.OsmiaConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class OsmiaClickGuiScreen extends Screen {
	private static final int BACKGROUND_DIM = 0x70000000;
	private static final int PANEL_START_X = 10;
	private static final int PANEL_START_Y = 10;
	private static final int PANEL_SPACING = 6;

	private final List<CategoryPanel> panels = new ArrayList<>();

	public OsmiaClickGuiScreen() {
		super(Component.literal("Osmia ClickGUI"));
	}

	@Override
	protected void init() {
		panels.clear();
		Category[] categories = Category.values();
		for (int index = 0; index < categories.length; index++) {
			Category category = categories[index];
			int x = PANEL_START_X + index * (CategoryPanel.WIDTH + PANEL_SPACING);
			CategoryPanel panel = new CategoryPanel(category.label, x, PANEL_START_Y);
			populate(panel, category);
			panels.add(panel);
		}
	}

	@Override
	public void extractRenderState(
			GuiGraphicsExtractor graphics,
			int mouseX,
			int mouseY,
			float partialTick
	) {
		graphics.fill(0, 0, width, height, BACKGROUND_DIM);
		for (CategoryPanel panel : panels) {
			panel.render(graphics, font, mouseX, mouseY);
		}
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		for (CategoryPanel panel : panels) {
			if (panel.mouseClicked(event.x(), event.y(), event.button())) {
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (OsmiaClient.matchesClickGuiKey(event)) {
			OsmiaClient.blockClickGuiOpenBriefly();
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static void populate(CategoryPanel panel, Category category) {
		if (category != Category.VISUAL) {
			return;
		}

		panel.addToggle("HUD", OsmiaConfig::isHudEnabled, OsmiaConfig::toggleHud);
		panel.addToggle("Hitbox", OsmiaConfig::isHitboxEnabled, OsmiaConfig::toggleHitbox)
				.addSetting(
						"Look Direction",
						OsmiaConfig::showsHitboxLookDirection,
						OsmiaConfig::toggleHitboxLookDirection
				)
				.addSetting(
						"Reach Distance",
						OsmiaConfig::showsHitboxReachDistance,
						OsmiaConfig::toggleHitboxReachDistance
				);
	}

	private enum Category {
		VISUAL("Visual"),
		PLAYER("Player"),
		MISC("Misc");

		private final String label;

		Category(String label) {
			this.label = label;
		}
	}
}
