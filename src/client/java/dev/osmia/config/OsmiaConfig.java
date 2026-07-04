package dev.osmia.config;

import dev.osmia.OsmiaClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class OsmiaConfig {
	private static final String FILE_NAME = "osmia_clickgui.properties";
	private static final String HUD_ENABLED = "hud.enabled";
	private static final String HITBOX_ENABLED = "hitbox.enabled";
	private static final String HITBOX_LOOK_DIRECTION = "hitbox.look_direction";
	private static final String HITBOX_REACH_DISTANCE = "hitbox.reach_distance";

	private static boolean loaded;
	private static boolean hudEnabled;
	private static boolean hitboxEnabled;
	private static boolean hitboxLookDirection;
	private static boolean hitboxReachDistance;

	private OsmiaConfig() {
	}

	public static void load() {
		if (loaded) {
			return;
		}
		loaded = true;

		Path file = configFile();
		if (!Files.isRegularFile(file)) {
			return;
		}

		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(file)) {
			properties.load(reader);
		} catch (IOException exception) {
			OsmiaClient.LOGGER.warn("Failed to load config from {}", file, exception);
			return;
		}

		hudEnabled = readBoolean(properties, HUD_ENABLED);
		hitboxEnabled = readBoolean(properties, HITBOX_ENABLED);
		hitboxLookDirection = readBoolean(properties, HITBOX_LOOK_DIRECTION);
		hitboxReachDistance = readBoolean(properties, HITBOX_REACH_DISTANCE);
	}

	public static boolean isHudEnabled() {
		ensureLoaded();
		return hudEnabled;
	}

	public static void toggleHud() {
		ensureLoaded();
		hudEnabled = !hudEnabled;
		save();
	}

	public static boolean isHitboxEnabled() {
		ensureLoaded();
		return hitboxEnabled;
	}

	public static void toggleHitbox() {
		ensureLoaded();
		hitboxEnabled = !hitboxEnabled;
		save();
	}

	public static boolean showsHitboxLookDirection() {
		ensureLoaded();
		return hitboxLookDirection;
	}

	public static void toggleHitboxLookDirection() {
		ensureLoaded();
		hitboxLookDirection = !hitboxLookDirection;
		save();
	}

	public static boolean showsHitboxReachDistance() {
		ensureLoaded();
		return hitboxReachDistance;
	}

	public static void toggleHitboxReachDistance() {
		ensureLoaded();
		hitboxReachDistance = !hitboxReachDistance;
		save();
	}

	private static void save() {
		Properties properties = new Properties();
		properties.setProperty(HUD_ENABLED, Boolean.toString(hudEnabled));
		properties.setProperty(HITBOX_ENABLED, Boolean.toString(hitboxEnabled));
		properties.setProperty(HITBOX_LOOK_DIRECTION, Boolean.toString(hitboxLookDirection));
		properties.setProperty(HITBOX_REACH_DISTANCE, Boolean.toString(hitboxReachDistance));

		Path file = configFile();
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file)) {
				properties.store(writer, "Osmia settings");
			}
		} catch (IOException exception) {
			OsmiaClient.LOGGER.warn("Failed to save config to {}", file, exception);
		}
	}

	private static boolean readBoolean(Properties properties, String key) {
		return Boolean.parseBoolean(properties.getProperty(key, "false"));
	}

	private static void ensureLoaded() {
		if (!loaded) {
			load();
		}
	}

	private static Path configFile() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}
}
