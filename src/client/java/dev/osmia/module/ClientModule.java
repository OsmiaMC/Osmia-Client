package dev.osmia.module;

import dev.osmia.config.OsmiaConfig;
import dev.osmia.module.setting.BooleanSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class ClientModule {
	private final String id;
	private final String displayName;
	private final ModuleCategory category;
	private final String enabledConfigKey;
	private final OsmiaConfig config;
	private final List<BooleanSetting> settings = new ArrayList<>();
	private boolean enabled;
	private boolean initialized;

	protected ClientModule(
			String id,
			String displayName,
			ModuleCategory category,
			boolean enabledByDefault,
			OsmiaConfig config
	) {
		this.id = Objects.requireNonNull(id, "id");
		this.displayName = Objects.requireNonNull(displayName, "displayName");
		this.category = Objects.requireNonNull(category, "category");
		this.config = Objects.requireNonNull(config, "config");
		enabledConfigKey = id + ".enabled";
		enabled = config.getBoolean(enabledConfigKey, enabledByDefault);
	}

	public final String id() {
		return id;
	}

	public final String displayName() {
		return displayName;
	}

	public final ModuleCategory category() {
		return category;
	}

	public final boolean isEnabled() {
		return enabled;
	}

	public final void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}

		this.enabled = enabled;
		config.setBoolean(enabledConfigKey, enabled);
		if (initialized) {
			if (enabled) {
				onEnable();
			} else {
				onDisable();
			}
		}
	}

	public final void toggle() {
		setEnabled(!enabled);
	}

	public final List<BooleanSetting> settings() {
		return List.copyOf(settings);
	}

	final void initialize(ModuleManager modules) {
		if (initialized) {
			return;
		}

		onInitialize(modules);
		initialized = true;
		if (enabled) {
			onEnable();
		}
	}

	protected final BooleanSetting booleanSetting(
			String id,
			String displayName,
			boolean defaultValue
	) {
		BooleanSetting setting = new BooleanSetting(
				id,
				displayName,
				this.id + "." + id,
				defaultValue,
				config
		);
		settings.add(setting);
		return setting;
	}

	protected void onInitialize(ModuleManager modules) {
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}
}
