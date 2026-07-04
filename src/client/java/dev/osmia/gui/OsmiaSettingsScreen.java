package dev.osmia.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class OsmiaSettingsScreen extends Screen {
	private static final Component TITLE = Component.literal("Osmia Settings");

	private final Screen parent;

	public OsmiaSettingsScreen(Screen parent) {
		super(TITLE);
		this.parent = parent;
	}

	@Override
	public void onClose() {
		minecraft.gui.setScreen(parent);
	}
}
