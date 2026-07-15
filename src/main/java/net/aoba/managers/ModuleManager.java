/*
 * Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.aoba.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants.Key;

import net.aoba.Aoba;
import net.aoba.api.IAddon;
import net.aoba.event.events.KeyDownEvent;
import net.aoba.event.listeners.KeyDownListener;
import net.aoba.gui.GuiManager;
import net.aoba.module.AntiCheat;
import net.aoba.module.Module;
import net.aoba.settings.Setting;
import net.aoba.settings.types.EnumSetting;
import net.minecraft.client.Minecraft;

public class ModuleManager implements KeyDownListener {
	private static final Minecraft MC = Minecraft.getInstance();

	public final ArrayList<Module> modules = new ArrayList<>();

	public EnumSetting<AntiCheat> antiCheat = EnumSetting.<AntiCheat>builder().id("aoba_anticheat")
			.displayName("Current AntiCheat")
			.description(
					"This setting will disable any modules or features that are known to be detected by a specific anticheat. ")
			.defaultValue(AntiCheat.Vanilla).onUpdate(s -> {
				for (Module module : modules) {
					if (module.isDetectable(s))
						module.state.setValue(false);
				}
			}).build();

	public ModuleManager(List<IAddon> addons) {
		if (addons != null) {
			addons.stream().filter(Objects::nonNull).map(IAddon::modules).filter(Objects::nonNull)
					.flatMap(List::stream).filter(Objects::nonNull).forEach(this::addModule);
		}

		// Registers all Module settings to the settings manager.
		for (Module module : modules) {
			for (Setting<?> setting : module.getSettings()) {
				SettingManager.registerSetting(setting);
			}
		}

		Aoba.getInstance().eventManager.AddListener(KeyDownListener.class, this);
	}

	public void addModule(Module module) {
		if (module != null && !modules.contains(module))
			modules.add(module);
	}

	public void disableAll() {
		for (Module module : modules) {
			module.state.setValue(false);
		}
	}

	public Module getModuleByName(String string) {
		for (Module module : modules) {
			if (module.getName().equalsIgnoreCase(string)) {
				return module;
			}
		}
		return null;
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (GuiManager.isKeyboardInputActive())
			return;
		if (event.GetKey() == GLFW.GLFW_KEY_UNKNOWN)
			return;

		if (MC.gui.screen() == null) {
			for (Module module : modules) {
				if (module.isDetectable(antiCheat.getValue()))
					continue;

				Key binding = module.getBind().getValue();
				if (binding.getValue() == GLFW.GLFW_KEY_UNKNOWN)
					continue;
				if (binding.getValue() == event.GetKey()) {
					module.toggle();
				}
			}
		}
	}
}
