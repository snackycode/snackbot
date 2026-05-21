package com.stattracker.application.service;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.MatchSummary;
import com.stattracker.domain.model.PlayerStats;
import com.stattracker.domain.port.in.StatsUseCase;
import com.stattracker.domain.port.out.GameApiClient;
import com.stattracker.domain.port.out.StatsCachePort;
import com.stattracker.application.factory.GameApiClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Core stats service — checks the cache first, falls through to the
 * game-specific API client on cache miss, then populates the cache.
 */
@Service
public class StatsService implements StatsUseCase {

    private static final Logger log = LoggerFactory.getLogger(StatsService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final GameApiClientFactory apiClientFactory;
    private final StatsCachePort statsCache;

    public StatsService(GameApiClientFactory apiClientFactory, StatsCachePort statsCache) {
        this.apiClientFactory = apiClientFactory;
        this.statsCache = statsCache;
    }

    @Override
    public PlayerStats getStats(Game game, String username, String region) {
        String cacheKey = StatsCachePort.buildKey(game.name(), username, region);

        Optional<PlayerStats> cached = statsCache.get(cacheKey);
        if (cached.isPresent()) {
            log.debug("Cache HIT for {}", cacheKey);
            return cached.get();
        }

        log.debug("Cache MISS for {} — calling {} API", cacheKey, game.getDisplayName());
        GameApiClient client = apiClientFactory.getClient(game);
        PlayerStats stats = client.fetchPlayerStats(username, region);

        statsCache.put(cacheKey, stats, CACHE_TTL);
        return stats;
    }

    @Override
    public List<MatchSummary> getRecentMatches(Game game, String username, String region, int count) {
        log.info("Fetching last {} matches for {} on {} ({})", count, username, game.getDisplayName(), region);
        GameApiClient client = apiClientFactory.getClient(game);
        return client.fetchRecentMatches(username, region, count);
    }
}
