package com.aetheris.client.module;

/**
 * Defines the functional categories for module organization.
 */
public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    PLAYER("Player"),
    FRIENDS("Friends"),
    ENEMIES("Enemies"),
    MISC("Miscellaneous"),
    CLIENT("Client"),
    SETTINGS("Settings");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
