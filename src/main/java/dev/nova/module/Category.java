package dev.nova.module;

public enum Category {
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	VISUALS("Visuals"),
	PLAYER("Player"),
	MISC("Misc");

	public final String title;

	Category(String title) {
		this.title = title;
	}
}
