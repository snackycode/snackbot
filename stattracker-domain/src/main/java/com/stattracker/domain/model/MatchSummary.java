package com.stattracker.domain.model;

import java.time.Instant;
import java.util.Map;

/**
 * Summary of a single completed match / game session.
 *
 * @param matchId     unique identifier from the game's API
 * @param game        which game this match belongs to
 * @param username    the player we're tracking
 * @param outcome     WIN, LOSS, or DRAW
 * @param kills       kills in this match
 * @param deaths      deaths in this match
 * @param assists     assists in this match
 * @param score       generic score metric (combat score, damage, placement…)
 * @param agentOrLegend  character/agent/legend played, empty string if N/A
 * @param mapName     map played on
 * @param durationSeconds match length in seconds
 * @param playedAt    when the match started (UTC)
 * @param metadata    arbitrary extra fields (e.g. "headshots", "placement")
 */
public record MatchSummary(
        String matchId,
        Game game,
        String username,
        Outcome outcome,
        int kills,
        int deaths,
        int assists,
        int score,
        String agentOrLegend,
        String mapName,
        int durationSeconds,
        Instant playedAt,
        Map<String, String> metadata
) {

    public enum Outcome {
        WIN, LOSS, DRAW
    }

    /**
     * Formatted KDA string like "12 / 5 / 8".
     */
    public String kdaString() {
        return kills + " / " + deaths + " / " + assists;
    }

    /**
     * Human-readable duration like "34m 12s".
     */
    public String durationDisplay() {
        int mins = durationSeconds / 60;
        int secs = durationSeconds % 60;
        return mins + "m " + secs + "s";
    }
}
