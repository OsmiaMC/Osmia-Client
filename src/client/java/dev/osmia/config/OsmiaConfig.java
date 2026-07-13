package dev.osmia.config;

import dev.osmia.OsmiaClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public final class OsmiaConfig {
	private final Path file;
	private final Properties values = new Properties();
	private boolean loaded;

	public OsmiaConfig(Path file) {
		this.file = Objects.requireNonNull(file, "file");
	}

	public void load() {
		if (loaded) {
			return;
		}
		loaded = true;

		if (!Files.isRegularFile(file)) {
			return;
		}

		try (Reader reader = Files.newBufferedReader(file)) {
			values.load(reader);
		} catch (IOException exception) {
			OsmiaClient.LOGGER.warn("Failed to load config from {}", file, exception);
		}
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		ensureLoaded();
		return Boolean.parseBoolean(values.getProperty(key, Boolean.toString(defaultValue)));
	}

	public void setBoolean(String key, boolean value) {
		ensureLoaded();
		String encoded = Boolean.toString(value);
		if (encoded.equals(values.getProperty(key))) {
			return;
		}
		values.setProperty(key, encoded);
		save();
	}

	private void save() {
		try {
			Path parent = file.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			try (Writer writer = Files.newBufferedWriter(file)) {
				values.store(writer, "Osmia settings");
			}
		} catch (IOException exception) {
			OsmiaClient.LOGGER.warn("Failed to save config to {}", file, exception);
		}
	}

	private void ensureLoaded() {
		if (!loaded) {
			load();
		}
	}
}
