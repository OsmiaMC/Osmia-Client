package dev.osmia.module.setting;

import dev.osmia.config.OsmiaConfig;

import java.util.Objects;

public final class BooleanSetting {
	private final String id;
	private final String displayName;
	private final String configKey;
	private final OsmiaConfig config;
	private boolean value;

	public BooleanSetting(
			String id,
			String displayName,
			String configKey,
			boolean defaultValue,
			OsmiaConfig config
	) {
		this.id = Objects.requireNonNull(id, "id");
		this.displayName = Objects.requireNonNull(displayName, "displayName");
		this.configKey = Objects.requireNonNull(configKey, "configKey");
		this.config = Objects.requireNonNull(config, "config");
		value = config.getBoolean(configKey, defaultValue);
	}

	public String id() {
		return id;
	}

	public String displayName() {
		return displayName;
	}

	public boolean value() {
		return value;
	}

	public void set(boolean value) {
		if (this.value == value) {
			return;
		}

		this.value = value;
		config.setBoolean(configKey, value);
	}

	public void toggle() {
		set(!value);
	}
}
