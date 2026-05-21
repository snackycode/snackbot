package com.stattracker.domain.model;

/**
 * Supported games tracked by the bot.
 * Each enum constant carries its display name and the region tags
 * that are valid for matchmaking lookups.
 */
public enum Game {

    VALORANT("Valorant", new String[]{"NA", "EU", "AP", "KR", "BR", "LATAM"}),
    LEAGUE("League of Legends", new String[]{"NA1", "EUW1", "EUN1", "KR", "JP1", "BR1", "LA1", "LA2", "OC1", "TR1", "RU"}),
    APEX("Apex Legends", new String[]{"PC", "PS4", "X1", "SWITCH"}),
    FORTNITE("Fortnite", new String[]{"PC", "CONSOLE", "TOUCH"});

    private final String displayName;
    private final String[] validRegions;

    Game(String displayName, String[] validRegions) {
        this.displayName = displayName;
        this.validRegions = validRegions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getValidRegions() {
        return validRegions.clone();
    }

    /**
     * Case-insensitive lookup by name or display name.
     *
     * @throws IllegalArgumentException if no match is found
     */
    public static Game fromString(String input) {
        for (Game game : values()) {
            if (game.name().equalsIgnoreCase(input) || game.displayName.equalsIgnoreCase(input)) {
                return game;
            }
        }
        throw new IllegalArgumentException("Unknown game: " + input);
    }

    /**
     * @return true when {@code region} is an accepted tag for this game
     */
    public boolean isValidRegion(String region) {
        for (String r : validRegions) {
            if (r.equalsIgnoreCase(region)) {
                return true;
            }
        }
        return false;
    }
}
