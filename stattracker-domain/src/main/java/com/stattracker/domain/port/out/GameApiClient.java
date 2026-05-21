package com.stattracker.domain.port.out;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.MatchSummary;
import com.stattracker.domain.model.PlayerStats;

import java.util.List;

/**
 * Secondary (driven) port for calling external game APIs.
 * Each supported game has its own implementation in the infrastructure layer.
 */
public interface GameApiClient {

    /**
     * @return the game this client handles
     */
    Game supportedGame();

    /**
     * Fetch current overall stats for a player.
     */
    PlayerStats fetchPlayerStats(String username, String region);

    /**
     * Fetch recent match history.
     *
     * @param count max number of matches to return
     */
    List<MatchSummary> fetchRecentMatches(String username, String region, int count);
}
