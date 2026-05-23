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

@Component
public class RiotApiClient implements GameApiClient {

    private static final Logger log = LoggerFactory.getLogger(RiotApiClient.class);

    private final WebClient riotWebClient;
    private final WebClient henrikWebClient;

    public RiotApiClient(@Qualifier("riotWebClient") WebClient riotWebClient, ExternalSecrets secrets) {
        this.riotWebClient  = riotWebClient;
        this.henrikWebClient = WebClient.builder()
                .baseUrl("https://api.henrikdev.xyz")
                .defaultHeader("Authorization", secrets.getHenrikApiKey())
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }

    @Override
    public Game supportedGame() {
        return Game.VALORANT;
    }

    @Override
    public PlayerStats fetchPlayerStats(String username, String region) {
        log.info("Fetching Valorant stats for {} ({})", username, region);

        String[] parts    = username.contains("#") ? username.split("#", 2) : new String[]{username, "NA1"};
        String gameName   = parts[0];
        String tagLine    = parts[1];

        // Resolve PUUID via official Riot API
        String puuid = resolvePuuid(gameName, tagLine);

        // We will pull the last 10 matches to aggregate stats and get current rank
        String rankTier = "Unranked";
        int rr = 0;
        int totalKills = 0;
        int totalDeaths = 0;
        int totalWins = 0;
        int totalMatches = 0;

        try {
            Map<?, ?> response = henrikWebClient.get()
                    .uri("/valorant/v4/matches/{region}/pc/{name}/{tag}?size=10",
                            region.toLowerCase(), gameName, tagLine)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("data")) {
                List<Map<?, ?>> matchList = (List<Map<?, ?>>) response.get("data");

                for (Map<?, ?> match : matchList) {
                    Map<?, ?> targetPlayer = findPlayer(match, gameName, tagLine);
                    
                    if (targetPlayer != null) {
                        // Capture rank from the most recent match only
                        if (totalMatches == 0) {
                            Map<?, ?> tier = (Map<?, ?>) targetPlayer.get("tier");
                            if (tier != null && tier.get("name") != null) {
                                rankTier = (String) tier.get("name");
                            }
                        }

                        // Aggregate stats
                        Map<?, ?> stats = (Map<?, ?>) targetPlayer.get("stats");
                        if (stats != null) {
                            totalKills += ((Number) stats.get("kills")).intValue();
                            totalDeaths += ((Number) stats.get("deaths")).intValue();
                        }
                        
                        String teamId = (String) targetPlayer.get("team_id");
                        if (MatchSummary.Outcome.WIN == resolveOutcome(match, teamId)) {
                            totalWins++;
                        }
                        
                        totalMatches++;
                    }
                }
            } else {
                log.warn("response is null or has no 'data' key.");
            }
        } catch (Exception e) {
            log.warn("Failed to fetch matches for stats from HenrikDev for {}: {}", username, e.getMessage(), e);
        }

        RankedInfo ranked = new RankedInfo(rankTier, "", rr, region);

        Map<String, String> extra = new LinkedHashMap<>();
        extra.put("PUUID", puuid);
        extra.put("Provider", "HenrikDev API");
        extra.put("Note", "Stats based on last " + totalMatches + " matches");
        
        log.info("End of Fetching for {} ({})", username, region);
        
        // Pass the aggregated stats into PlayerStats.of()
        return PlayerStats.of(Game.VALORANT, username, region,
                0, totalKills, totalDeaths, totalWins, totalMatches, ranked, extra);
    }

    // ─────────────────────────────────────────────
    // fetchRecentMatches — real data from HenrikDev
    // ─────────────────────────────────────────────
    @Override
    public List<MatchSummary> fetchRecentMatches(String username, String region, int count) {
        log.info("Fetching last {} Valorant matches for {} ({})", count, username, region);

        String[] parts  = username.contains("#") ? username.split("#", 2) : new String[]{username, "NA1"};
        String gameName = parts[0];
        String tagLine  = parts[1];

        List<MatchSummary> matches = new ArrayList<>();

        try {
            Map<?, ?> response = henrikWebClient.get()
                    .uri("/valorant/v4/matches/{region}/pc/{name}/{tag}?size={count}",
                            region.toLowerCase(), gameName, tagLine, count)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("data")) {
                log.warn("No match data returned for {}", username);
                return matches;
            }

            List<Map<?, ?>> matchList = (List<Map<?, ?>>) response.get("data");

            for (Map<?, ?> match : matchList) {

                // ── metadata ──
                Map<?, ?> metadata     = (Map<?, ?>) match.get("metadata");
                String    mapName      = ((Map<?, ?>) metadata.get("map")).get("name").toString();
                long      gameLengthMs = ((Number) metadata.get("game_length_in_ms")).longValue();
                String    startedAt    = (String) metadata.get("started_at");

                // ── find the requested player ──
                Map<?, ?> targetPlayer = findPlayer(match, gameName, tagLine);
                if (targetPlayer == null) {
                    log.warn("Player {}#{} not found in match, skipping", gameName, tagLine);
                    continue;
                }

                String playerName = (String) targetPlayer.get("name");
                String playerTag  = (String) targetPlayer.get("tag");
                String teamId     = (String) targetPlayer.get("team_id");

                // ── agent ──
                Map<?, ?> agent   = (Map<?, ?>) targetPlayer.get("agent");
                String agentName  = (String) agent.get("name");

                // ── tier ──
                Map<?, ?> tier    = (Map<?, ?>) targetPlayer.get("tier");
                String tierName   = (String) tier.get("name");

                // ── stats ──
                Map<?, ?> stats   = (Map<?, ?>) targetPlayer.get("stats");
                int score         = ((Number) stats.get("score")).intValue();
                int kills         = ((Number) stats.get("kills")).intValue();
                int deaths        = ((Number) stats.get("deaths")).intValue();
                int assists       = ((Number) stats.get("assists")).intValue();
                int headshots     = ((Number) stats.get("headshots")).intValue();
                int bodyshots     = ((Number) stats.get("bodyshots")).intValue();
                int legshots      = ((Number) stats.get("legshots")).intValue();

                Map<?, ?> damage  = (Map<?, ?>) stats.get("damage");
                int dealt         = ((Number) damage.get("dealt")).intValue();
                int received      = ((Number) damage.get("received")).intValue();

                // ── win/loss from teams block ──
                MatchSummary.Outcome outcome = resolveOutcome(match, teamId);

                matches.add(new MatchSummary(
                        UUID.randomUUID().toString(),
                        Game.VALORANT,
                        playerName + "#" + playerTag,
                        outcome,
                        kills,
                        deaths,
                        assists,
                        score,
                        agentName,
                        mapName,
                        (int) (gameLengthMs / 1000),
                        Instant.parse(startedAt),
                        Map.of(
                                "Tier",      tierName,
                                "Headshots", String.valueOf(headshots),
                                "Bodyshots", String.valueOf(bodyshots),
                                "Legshots",  String.valueOf(legshots),
                                "Dealt",     String.valueOf(dealt),
                                "Received",  String.valueOf(received)
                        )
                ));
            }

        } catch (Exception e) {
            log.error("Failed to fetch recent matches for {}: {}", username, e.getMessage(), e);
        }

        return matches;
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private String resolvePuuid(String gameName, String tagLine) {
        try {
            Map<?, ?> account = riotWebClient.get()
                    .uri("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}", gameName, tagLine)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (account != null && account.containsKey("puuid")) {
                return (String) account.get("puuid");
            }
        } catch (Exception e) {
            log.warn("PUUID resolution failed for {}#{}: {}", gameName, tagLine, e.getMessage());
        }
        return "unknown";
    }

    /** Finds the target player by name+tag inside a match's players list. */
    private Map<?, ?> findPlayer(Map<?, ?> match, String gameName, String tagLine) {
        List<Map<?, ?>> players = (List<Map<?, ?>>) match.get("players");
        if (players == null) return null;

        return players.stream()
                .filter(p -> gameName.equalsIgnoreCase((String) p.get("name"))
                        && tagLine.equalsIgnoreCase((String) p.get("tag")))
                .findFirst()
                .orElse(null);
    }

    /** Resolves WIN/LOSS by looking up the player's team in the teams block. */
    private MatchSummary.Outcome resolveOutcome(Map<?, ?> match, String teamId) {
        try {
            List<Map<?, ?>> teams = (List<Map<?, ?>>) match.get("teams");
            if (teams == null) return MatchSummary.Outcome.LOSS;

            return teams.stream()
                    .filter(t -> teamId.equalsIgnoreCase((String) t.get("team_id")))
                    .findFirst()
                    .map(t -> Boolean.TRUE.equals(t.get("won"))
                            ? MatchSummary.Outcome.WIN
                            : MatchSummary.Outcome.LOSS)
                    .orElse(MatchSummary.Outcome.LOSS);
        } catch (Exception e) {
            log.warn("Could not resolve outcome for team {}: {}", teamId, e.getMessage());
            return MatchSummary.Outcome.LOSS;
        }
    }
}