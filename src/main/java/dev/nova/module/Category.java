package dev.nova.module;

public enum Category {
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	RENDER("Render"),
	PLAYER("Player"),
	WORLD("World");

	public final String title;

	Category(String title) {
		this.title = title;
	}
}
