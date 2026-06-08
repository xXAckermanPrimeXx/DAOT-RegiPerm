package com.zenyssevernox.regiperm.data;

public enum Rank {
    COMMANDER("Commander"),
    SECTION_COMMANDER("Section Commander"),
    CAPTAIN("Captain"),
    LIEUTENANT("Lieutenant"),
    SERGEANT("Sergeant"),
    CORPORAL("Corporal"),
    LANCE_CORPORAL("Lance Corporal"),
    PRIVATE_FIRST_CLASS("Private First Class"),
    PRIVATE("Private"),
    RECRUIT("Recruit");

    private final String displayName;

    Rank(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Rank fromString(String input) {
        String cleaned = input.trim().toUpperCase().replace(" ", "_");

        for (Rank rank : values()) {
            if (rank.name().equals(cleaned)) {
                return rank;
            }
        }

        return null;
    }

    public boolean isHighCommand() {
        return this == COMMANDER || this == SECTION_COMMANDER;
    }
}