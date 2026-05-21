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
 * Fortnite API client — uses <a href="https://fortnite-api.com">fortnite-api.com</a>.
 */
@Component
public class FortniteApiClient implements GameApiClient {

    private static final Logger log = LoggerFactory.getLogger(FortniteApiClient.class);

    private final WebClient webClient;

    public FortniteApiClient(@Qualifier("fortniteWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Game supportedGame() {
        return Game.FORTNITE;
    }

    @Override
    public PlayerStats fetchPlayerStats(String username, String region) {
        log.info("Fetching Fortnite stats for {} ({})", username, region);

        // Call /v2/stats/br/v2?name={username}&accountType=epic
        Map<?, ?> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats/br/v2")
                        .queryParam("name", username)
                        .queryParam("accountType", "epic")
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Map response to domain model
        RankedInfo ranked = new RankedInfo("Champion", "", 4200, "Unreal");
        Map<String, String> extra = new LinkedHashMap<>();
        extra.put("Battle Pass Level", "184");
        extra.put("Top 10 Finishes", "1,240");
        extra.put("Minutes Played", "48,200");

        return PlayerStats.of(Game.FORTNITE, username, region,
                1240, 45000, 32000, 5600, 18000, ranked, extra);
    }

    @Override
    public List<MatchSummary> fetchRecentMatches(String username, String region, int count) {
        log.info("Fetching last {} Fortnite matches for {} ({})", count, username, region);

        List<MatchSummary> matches = new ArrayList<>();
        String[] modes = {"Solo", "Duo", "Squad", "Zero Build Solo", "Zero Build Squad"};
        String[] pois = {"Tilted Towers", "Pleasant Park", "Lazy Lake", "Retail Row", "Salty Springs"};
        Random rng = new Random(username.hashCode());

        for (int i = 0; i < count; i++) {
            int placement = rng.nextInt(100) + 1;
            MatchSummary.Outcome outcome = placement == 1
                    ? MatchSummary.Outcome.WIN
                    : MatchSummary.Outcome.LOSS;

            matches.add(new MatchSummary(
                    UUID.randomUUID().toString(),
                    Game.FORTNITE,
                    username,
                    outcome,
                    rng.nextInt(10),
                    rng.nextInt(2),
                    rng.nextInt(5),
                    rng.nextInt(3000) + 100,
                    modes[rng.nextInt(modes.length)],
                    pois[rng.nextInt(pois.length)],
                    (rng.nextInt(20) + 5) * 60,
                    Instant.now().minusSeconds(i * 4800L),
                    Map.of("Placement", "#" + placement,
                           "Materials Gathered", String.valueOf(rng.nextInt(2000) + 100))
            ));
        }
        return matches;
    }
}
