package com.stattracker.domain.model;

/**
 * A player's current competitive / ranked standing.
 *
 * @param tier     rank tier (e.g. "Gold", "Diamond", "Predator", "Champion")
 * @param rank     division within the tier (e.g. "II", "3", or "" when not applicable)
 * @param points   league points, ranked rating, or RP depending on the game
 * @param peakTier highest tier ever achieved, may be null
 */
public record RankedInfo(
        String tier,
        String rank,
        int points,
        String peakTier
) {

    /**
     * Human-readable label, e.g. "Diamond II — 76 LP".
     */
    public String displayString() {
        String base = rank.isBlank() ? tier : tier + " " + rank;
        return base + " — " + points + " pts";
    }

    /**
     * @return true when the player is currently placed in a ranked queue
     */
    public boolean isPlaced() {
        return tier != null && !tier.isBlank();
    }
}
