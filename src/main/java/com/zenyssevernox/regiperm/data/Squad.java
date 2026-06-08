package com.zenyssevernox.regiperm.data;

import net.minecraft.util.Formatting;

public enum Squad {
    LEVI_SQUAD("Levi Squad", Regiment.SCOUTS, Formatting.DARK_BLUE),
    REAR_GUARD("Rear Guard", Regiment.GARRISON, Formatting.DARK_RED),
    INTERNAL_FORCE("Internal Force", Regiment.MILITARY_POLICE, Formatting.DARK_GREEN),
    NONE("None", Regiment.NONE, Formatting.WHITE);

    private final String displayName;
    private final Regiment requiredRegiment;
    private final Formatting color;

    Squad(String displayName, Regiment requiredRegiment, Formatting color) {
        this.displayName = displayName;
        this.requiredRegiment = requiredRegiment;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Regiment getRequiredRegiment() {
        return requiredRegiment;
    }

    public Formatting getColor() {
        return color;
    }

    public static Squad fromString(String input) {
        String cleaned = input.trim().toUpperCase().replace(" ", "_");

        for (Squad squad : values()) {
            if (squad.name().equals(cleaned)) {
                return squad;
            }
        }

        return null;
    }
}