package com.stattracker.domain.port.in;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.MatchSummary;
import com.stattracker.domain.model.PlayerStats;

import java.util.List;

/**
 * Primary (driving) port for fetching player statistics.
 * Implemented by the application-layer {@code StatsService}.
 */
public interface StatsUseCase {

    /**
     * Retrieve the current stats for a player.
     *
     * @param game     the game to query
     * @param username the player's in-game name (Riot ID, EA ID, Epic name, etc.)
     * @param region   region/platform tag
     * @return normalized player statistics
     */
    PlayerStats getStats(Game game, String username, String region);

    /**
     * Fetch the most recent match history for a player.
     *
     * @param game     the game to query
     * @param username player's in-game name
     * @param region   region/platform tag
     * @param count    maximum number of matches to return
     * @return list of match summaries, most recent first
     */
    List<MatchSummary> getRecentMatches(Game game, String username, String region, int count);
}
