package dev.osmia.module;

public enum ModuleCategory {
	VISUAL("Visual"),
	PLAYER("Player"),
	MOVEMENT("Movement"),
	MISC("Misc");

	private final String displayName;

	ModuleCategory(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return displayName;
	}
}
