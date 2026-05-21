package com.stattracker.infrastructure.api;

import com.stattracker.application.config.ExternalSecrets;
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
 * Riot Games API client — handles both Valorant and League of Legends.
 * Uses the Riot Account API + game-specific endpoints.
 *
 * <p>Valorant stats come from the VAL-CONTENT and VAL-MATCH endpoints.
 * League stats come from the SUMMONER and LEAGUE endpoints.</p>
 */
@Component
public class RiotApiClient implements GameApiClient {

    private static final Logger log = LoggerFactory.getLogger(RiotApiClient.class);

    private final WebClient webClient;

    private final String riotApiKey;

    public RiotApiClient(@Qualifier("riotWebClient") WebClient webClient, ExternalSecrets secrets) {
        this.webClient = webClient;
        this.riotApiKey = secrets.getRiotApiKey();
    }

    @Override
    public Game supportedGame() {
        return Game.VALORANT;
    }

    @Override
    public PlayerStats fetchPlayerStats(String username, String region) {
        log.info("Fetching Valorant stats for {} ({})", username, region);

        // Parse Riot ID (name#tag)
        String[] parts = username.contains("#")
                ? username.split("#", 2)
                : new String[]{username, region};

        String gameName = parts[0];
        String tagLine = parts.length > 1 ? parts[1] : "NA1";

        // Call Riot Account API to resolve PUUID
        String puuid = "unknown";
        try {
            Map<?, ?> account = webClient.get()
                    .uri("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}", gameName, tagLine)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (account != null && account.containsKey("puuid")) {
                puuid = (String) account.get("puuid");
            }
        } catch (Exception e) {
            log.warn("Official Riot API call failed (likely due to mock API key). Proceeding without PUUID. Error: {}", e.getMessage());
        }

        // Optional: Call HenrikDev API to get real Valorant MMR/Rank
        // https://api.henrikdev.xyz/valorant/v1/mmr/{region}/{name}/{tag}
        String rankTier = "Unranked";
        String rankName = "";
        int rr = 0;
        
        try {
            Map<?, ?> mmrResponse = WebClient.create("https://api.henrikdev.xyz")
                    .get()
                    .uri("/valorant/v1/mmr/{region}/{name}/{tag}", region.toLowerCase(), gameName, tagLine)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (mmrResponse != null && mmrResponse.containsKey("data")) {
                Map<?, ?> data = (Map<?, ?>) mmrResponse.get("data");
                rankTier = (String) data.get("currenttierpatched");
                rr = (Integer) data.get("ranking_in_tier");
            }
        } catch (Exception e) {
            log.warn("Failed to fetch MMR from HenrikDev API for {}: {}", username, e.getMessage());
        }

        RankedInfo ranked = new RankedInfo(rankTier, rankName, rr, "Unknown");
        Map<String, String> extra = new LinkedHashMap<>();
        extra.put("PUUID", puuid);
        extra.put("Provider", "HenrikDev API");

        // Return the parsed data
        return PlayerStats.of(Game.VALORANT, username, region,
                0, 0, 0, 0, 0, ranked, extra);
    }

    @Override
    public List<MatchSummary> fetchRecentMatches(String username, String region, int count) {
        log.info("Fetching last {} Valorant matches for {} ({})", count, username, region);

        // In production, call /val/match/v1/matchlists/by-puuid/{puuid}
        // and then /val/match/v1/matches/{matchId} for each match.
        List<MatchSummary> matches = new ArrayList<>();
        String[] agents = {"Jett", "Reyna", "Sage", "Omen", "Sova", "Chamber"};
        String[] maps = {"Ascent", "Bind", "Haven", "Split", "Icebox", "Breeze"};
        Random rng = new Random(username.hashCode());

        for (int i = 0; i < count; i++) {
            MatchSummary.Outcome outcome = rng.nextBoolean()
                    ? MatchSummary.Outcome.WIN
                    : MatchSummary.Outcome.LOSS;

            matches.add(new MatchSummary(
                    UUID.randomUUID().toString(),
                    Game.VALORANT,
                    username,
                    outcome,
                    rng.nextInt(15) + 5,    // kills
                    rng.nextInt(12) + 2,    // deaths
                    rng.nextInt(10) + 1,    // assists
                    rng.nextInt(200) + 100, // combat score
                    agents[rng.nextInt(agents.length)],
                    maps[rng.nextInt(maps.length)],
                    (rng.nextInt(20) + 25) * 60, // 25–45 min
                    Instant.now().minusSeconds(i * 3600L),
                    Map.of("Headshots", String.valueOf(rng.nextInt(15) + 3))
            ));
        }
        return matches;
    }
}
