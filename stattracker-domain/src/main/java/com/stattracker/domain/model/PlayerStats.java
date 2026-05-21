package com.stattracker.domain.model;

import java.time.Instant;
import java.util.Map;

/**
 * Normalized player statistics that work across every supported game.
 * Each game-specific API adapter maps its raw response into this record.
 *
 * @param game          the game these stats belong to
 * @param username      in-game display name (e.g. Riot ID, EA ID, Epic name)
 * @param region        server/platform region
 * @param level         account level (0 when unavailable)
 * @param totalKills    lifetime kills
 * @param totalDeaths   lifetime deaths
 * @param totalWins     lifetime wins
 * @param totalMatches  lifetime matches played
 * @param kdRatio       kills / deaths ratio
 * @param winRate       wins / matches ratio (0.0–1.0)
 * @param rankedInfo    current ranked standing, nullable
 * @param extra         game-specific fields not covered above (e.g. "headshot%", "damage/round")
 * @param fetchedAt     UTC timestamp when the data was retrieved
 */
public record PlayerStats(
        Game game,
        String username,
        String region,
        int level,
        long totalKills,
        long totalDeaths,
        long totalWins,
        long totalMatches,
        double kdRatio,
        double winRate,
        RankedInfo rankedInfo,
        Map<String, String> extra,
        Instant fetchedAt
) {

    /**
     * Convenience factory that auto-computes KD and win-rate.
     */
    public static PlayerStats of(Game game, String username, String region,
                                  int level, long kills, long deaths,
                                  long wins, long matches,
                                  RankedInfo ranked, Map<String, String> extra) {
        double kd = deaths == 0 ? kills : (double) kills / deaths;
        double wr = matches == 0 ? 0.0 : (double) wins / matches;
        return new PlayerStats(game, username, region, level,
                kills, deaths, wins, matches, kd, wr, ranked, extra, Instant.now());
    }
}
