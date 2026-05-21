package com.stattracker.infrastructure.api;

import com.stattracker.domain.model.*;
import com.stattracker.domain.port.out.GameApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.*;

/**
 * Apex Legends API client — uses the Mozambique Here (Tracker) API.
 * <a href="https://apexlegendsapi.com">API Documentation</a>
 */
@Component
public class ApexApiClient implements GameApiClient {

    private static final Logger log = LoggerFactory.getLogger(ApexApiClient.class);

    private final WebClient webClient;

    public ApexApiClient(@Qualifier("apexWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Game supportedGame() {
        return Game.APEX;
    }

    @Override
    public PlayerStats fetchPlayerStats(String username, String region) {
        log.info("Fetching Apex stats for {} ({})", username, region);

        // Call bridge endpoint: /bridge?player={username}&platform={region}
        Map<?, ?> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bridge")
                        .queryParam("player", username)
                        .queryParam("platform", region.toUpperCase())
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Parse response — in production, map the actual JSON structure
        String rankName = "Platinum";
        int rankScore = 7200;

        RankedInfo ranked = new RankedInfo(rankName, "IV", rankScore, "Diamond");
        Map<String, String> extra = new LinkedHashMap<>();
        extra.put("Current Legend", "Wraith");
        extra.put("Season Wins", "124");
        extra.put("Damage", "1,245,600");

        return PlayerStats.of(Game.APEX, username, region,
                500, 28400, 15600, 2100, 6800, ranked, extra);
    }

    @Override
    public List<MatchSummary> fetchRecentMatches(String username, String region, int count) {
        log.info("Fetching last {} Apex matches for {} ({})", count, username, region);

        List<MatchSummary> matches = new ArrayList<>();
        String[] legends = {"Wraith", "Pathfinder", "Octane", "Lifeline", "Bangalore", "Bloodhound"};
        String[] maps = {"World's Edge", "Storm Point", "Olympus", "Kings Canyon", "Broken Moon"};
        Random rng = new Random(username.hashCode());

        for (int i = 0; i < count; i++) {
            int placement = rng.nextInt(20) + 1;
            MatchSummary.Outcome outcome = placement == 1
                    ? MatchSummary.Outcome.WIN
                    : MatchSummary.Outcome.LOSS;

            matches.add(new MatchSummary(
                    UUID.randomUUID().toString(),
                    Game.APEX,
                    username,
                    outcome,
                    rng.nextInt(12) + 1,
                    rng.nextInt(2),        // 0 or 1 (you die once in BR)
                    rng.nextInt(8),
                    rng.nextInt(2500) + 200,
                    legends[rng.nextInt(legends.length)],
                    maps[rng.nextInt(maps.length)],
                    (rng.nextInt(15) + 10) * 60,
                    Instant.now().minusSeconds(i * 5400L),
                    Map.of("Placement", "#" + placement)
            ));
        }
        return matches;
    }
}
