package dev.osmia.module.impl.visual;

import dev.osmia.config.OsmiaConfig;
import dev.osmia.module.ClientModule;
import dev.osmia.module.ModuleCategory;
import dev.osmia.module.ModuleManager;
import dev.osmia.module.setting.BooleanSetting;
import dev.osmia.render.HitboxRenderer;

public final class HitboxModule extends ClientModule {
	private final BooleanSetting lookDirection;
	private final BooleanSetting reachDistance;

	public HitboxModule(OsmiaConfig config) {
		super("hitbox", "Hitbox", ModuleCategory.VISUAL, false, config);
		lookDirection = booleanSetting("look_direction", "Look Direction", false);
		reachDistance = booleanSetting("reach_distance", "Reach Distance", false);
	}

	public BooleanSetting lookDirection() {
		return lookDirection;
	}

	public BooleanSetting reachDistance() {
		return reachDistance;
	}

	@Override
	protected void onInitialize(ModuleManager modules) {
		new HitboxRenderer(this).register();
	}
}
