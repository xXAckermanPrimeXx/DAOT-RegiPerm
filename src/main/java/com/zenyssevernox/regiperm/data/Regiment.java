package com.zenyssevernox.regiperm.data;

import net.minecraft.util.Formatting;

public enum Regiment {
    SCOUTS("Scouts", Formatting.AQUA),
    GARRISON("Garrison", Formatting.RED),
    MILITARY_POLICE("Military Police", Formatting.GREEN),
    NONE("None", Formatting.WHITE);

    private final String displayName;
    private final Formatting color;

    Regiment(String displayName, Formatting color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Formatting getColor() {
        return color;
    }

    public static Regiment fromString(String input) {
        String cleaned = input.trim().toUpperCase().replace(" ", "_");

        if (cleaned.equals("MP")) {
            return MILITARY_POLICE;
        }

        for (Regiment regiment : values()) {
            if (regiment.name().equals(cleaned)) {
                return regiment;
            }
        }

        return null;
    }
}