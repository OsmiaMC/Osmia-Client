package dev.osmia.module.impl.movement;

import dev.osmia.config.OsmiaConfig;
import dev.osmia.module.ClientModule;
import dev.osmia.module.ModuleCategory;

public final class AutoSprintModule extends ClientModule {
	public AutoSprintModule(OsmiaConfig config) {
		super("auto_sprint", "Auto Sprint", ModuleCategory.MOVEMENT, false, config);
	}
}
