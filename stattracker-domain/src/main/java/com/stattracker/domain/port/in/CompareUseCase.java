package com.stattracker.domain.port.in;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.PlayerStats;

/**
 * Primary port for comparing two players' stats side-by-side.
 */
public interface CompareUseCase {

    /**
     * Value object holding two players' stats for comparison.
     */
    record ComparisonResult(
            PlayerStats playerA,
            PlayerStats playerB,
            String summary
    ) {
    }

    /**
     * Compare two players in the same game.
     *
     * @param game      game to compare in
     * @param usernameA first player's in-game name
     * @param regionA   first player's region
     * @param usernameB second player's in-game name
     * @param regionB   second player's region
     * @return comparison result with both stat sets and a human-readable summary
     */
    ComparisonResult compare(Game game,
                             String usernameA, String regionA,
                             String usernameB, String regionB);
}
