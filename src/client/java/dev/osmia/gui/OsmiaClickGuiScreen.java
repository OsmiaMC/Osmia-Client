package dev.osmia.gui;

import dev.osmia.OsmiaClient;
import dev.osmia.module.ClientModule;
import dev.osmia.module.ModuleCategory;
import dev.osmia.module.ModuleManager;
import dev.osmia.module.setting.BooleanSetting;
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

	private final ModuleManager modules;
	private final List<CategoryPanel> panels = new ArrayList<>();

	public OsmiaClickGuiScreen(ModuleManager modules) {
		super(Component.literal("Osmia ClickGUI"));
		this.modules = modules;
	}

	@Override
	protected void init() {
		panels.clear();
		ModuleCategory[] categories = ModuleCategory.values();
		for (int index = 0; index < categories.length; index++) {
			ModuleCategory category = categories[index];
			int x = PANEL_START_X + index * (CategoryPanel.WIDTH + PANEL_SPACING);
			CategoryPanel panel = new CategoryPanel(category.displayName(), x, PANEL_START_Y);
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

	private void populate(CategoryPanel panel, ModuleCategory category) {
		for (ClientModule module : modules.inCategory(category)) {
			CategoryPanel.ToggleRow row = panel.addToggle(
					module.displayName(),
					module::isEnabled,
					module::toggle
			);
			for (BooleanSetting setting : module.settings()) {
				row.addSetting(setting.displayName(), setting::value, setting::toggle);
			}
		}
	}
}
