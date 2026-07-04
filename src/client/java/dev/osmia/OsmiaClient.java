package dev.osmia;

import com.mojang.blaze3d.platform.InputConstants;
import dev.osmia.config.OsmiaConfig;
import dev.osmia.gui.OsmiaClickGuiScreen;
import dev.osmia.profile.PvpSettingsProfile;
import dev.osmia.render.HitboxRenderer;
import dev.osmia.render.OsmiaHud;
import dev.osmia.render.OsmiaRoundedRect;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OsmiaClient implements ClientModInitializer {
	public static final String MOD_ID = "osmia";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final long CLICK_GUI_REOPEN_DELAY_NANOS = 250_000_000L;
	private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(MOD_ID, "general")
	);

	private static KeyMapping clickGuiKey;
	private static long clickGuiOpenBlockedUntil;
	private boolean profileApplied;

	@Override
	public void onInitializeClient() {
		OsmiaConfig.load();
		OsmiaRoundedRect.initialize();
		OsmiaHud.initialize();
		HitboxRenderer.initialize();
		registerKeyMapping();
		registerClientTick();

		LOGGER.info("Osmia initialized");
	}

	public static boolean matchesClickGuiKey(KeyEvent event) {
		return clickGuiKey != null && clickGuiKey.matches(event);
	}

	public static void blockClickGuiOpenBriefly() {
		clickGuiOpenBlockedUntil = System.nanoTime() + CLICK_GUI_REOPEN_DELAY_NANOS;
	}

	private static void registerKeyMapping() {
		clickGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.osmia.open_click_gui",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				KEY_CATEGORY
		));
	}

	private void registerClientTick() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!profileApplied) {
				int changed = PvpSettingsProfile.apply(client.options);
				profileApplied = true;
				LOGGER.info("PvP settings profile applied ({} settings changed)", changed);
			}

			while (clickGuiKey.consumeClick()) {
				if (client.gui.screen() == null && System.nanoTime() >= clickGuiOpenBlockedUntil) {
					client.gui.setScreen(new OsmiaClickGuiScreen());
				}
			}
		});
	}
}
