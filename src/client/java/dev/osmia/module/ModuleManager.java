package dev.osmia.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ModuleManager {
	private final List<ClientModule> modules = new ArrayList<>();
	private final Map<String, ClientModule> modulesById = new HashMap<>();
	private final Map<Class<? extends ClientModule>, ClientModule> modulesByType = new HashMap<>();
	private boolean initializationStarted;
	private boolean initialized;

	public <T extends ClientModule> T register(T module) {
		Objects.requireNonNull(module, "module");
		if (initializationStarted) {
			throw new IllegalStateException("Cannot register modules after initialization has started");
		}
		if (modulesById.containsKey(module.id())) {
			throw new IllegalArgumentException("Duplicate module id: " + module.id());
		}
		if (modulesByType.containsKey(module.getClass())) {
			throw new IllegalArgumentException("Duplicate module type: " + module.getClass().getName());
		}

		modules.add(module);
		modulesById.put(module.id(), module);
		modulesByType.put(module.getClass(), module);
		return module;
	}

	public void initialize() {
		if (initialized) {
			return;
		}
		if (initializationStarted) {
			throw new IllegalStateException("Module initialization did not complete");
		}
		initializationStarted = true;

		for (ClientModule module : modules) {
			module.initialize(this);
		}
		initialized = true;
	}

	public List<ClientModule> all() {
		return List.copyOf(modules);
	}

	public List<ClientModule> inCategory(ModuleCategory category) {
		Objects.requireNonNull(category, "category");
		return modules.stream()
				.filter(module -> module.category() == category)
				.toList();
	}

	public <T extends ClientModule> T get(Class<T> type) {
		Objects.requireNonNull(type, "type");
		ClientModule module = modulesByType.get(type);
		if (module == null) {
			throw new IllegalArgumentException("Module is not registered: " + type.getName());
		}
		return type.cast(module);
	}
}
