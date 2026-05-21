package com.stattracker.application.service;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.PlayerStats;
import com.stattracker.domain.port.in.CompareUseCase;
import com.stattracker.domain.port.in.StatsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Compares two players' stats and produces a summary.
 * Delegates actual stat fetching to {@link StatsUseCase} (which handles caching).
 */
@Service
public class CompareService implements CompareUseCase {

    private static final Logger log = LoggerFactory.getLogger(CompareService.class);

    private final StatsUseCase statsUseCase;

    public CompareService(StatsUseCase statsUseCase) {
        this.statsUseCase = statsUseCase;
    }

    @Override
    public ComparisonResult compare(Game game,
                                     String usernameA, String regionA,
                                     String usernameB, String regionB) {
        log.info("Comparing {} ({}) vs {} ({}) on {}",
                usernameA, regionA, usernameB, regionB, game.getDisplayName());

        PlayerStats a = statsUseCase.getStats(game, usernameA, regionA);
        PlayerStats b = statsUseCase.getStats(game, usernameB, regionB);

        String summary = buildSummary(a, b);
        return new ComparisonResult(a, b, summary);
    }

    private String buildSummary(PlayerStats a, PlayerStats b) {
        StringBuilder sb = new StringBuilder();
        sb.append("**").append(a.username()).append("** vs **").append(b.username()).append("**\n\n");

        sb.append(compareField("K/D Ratio", a.kdRatio(), b.kdRatio()));
        sb.append(compareField("Win Rate", a.winRate() * 100, b.winRate() * 100, "%"));
        sb.append(compareField("Total Kills", a.totalKills(), b.totalKills()));
        sb.append(compareField("Total Wins", a.totalWins(), b.totalWins()));
        sb.append(compareField("Total Matches", a.totalMatches(), b.totalMatches()));

        if (a.rankedInfo() != null && b.rankedInfo() != null) {
            sb.append("\n**Ranked**: ")
              .append(a.rankedInfo().displayString())
              .append(" vs ")
              .append(b.rankedInfo().displayString());
        }

        return sb.toString();
    }

    private String compareField(String label, double valA, double valB) {
        return compareField(label, valA, valB, "");
    }

    private String compareField(String label, double valA, double valB, String suffix) {
        String winner = valA > valB ? " ◀" : valA < valB ? " ▶" : " ≡";
        return String.format("• **%s**: %.2f%s vs %.2f%s%s\n",
                label, valA, suffix, valB, suffix, winner);
    }
}
