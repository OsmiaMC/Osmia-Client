package dev.osmia.module.impl.visual;

import dev.osmia.config.OsmiaConfig;
import dev.osmia.module.ClientModule;
import dev.osmia.module.ModuleCategory;
import dev.osmia.module.ModuleManager;
import dev.osmia.render.OsmiaHud;

public final class HudModule extends ClientModule {
	public HudModule(OsmiaConfig config) {
		super("hud", "HUD", ModuleCategory.VISUAL, false, config);
	}

	@Override
	protected void onInitialize(ModuleManager modules) {
		new OsmiaHud(this).register();
	}
}
