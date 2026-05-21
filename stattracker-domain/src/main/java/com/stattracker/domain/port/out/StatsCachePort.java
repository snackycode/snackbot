package com.stattracker.domain.port.out;

import com.stattracker.domain.model.PlayerStats;

import java.time.Duration;
import java.util.Optional;

/**
 * Secondary (driven) port for caching player statistics.
 * Implemented by the infrastructure layer's Redis adapter.
 */
public interface StatsCachePort {

    /**
     * Retrieve cached stats if still valid.
     *
     * @param cacheKey composite key (e.g. "VALORANT:username:NA")
     * @return cached stats or empty
     */
    Optional<PlayerStats> get(String cacheKey);

    /**
     * Store stats in the cache.
     *
     * @param cacheKey composite key
     * @param stats    data to cache
     * @param ttl      time-to-live before expiry
     */
    void put(String cacheKey, PlayerStats stats, Duration ttl);

    /**
     * Evict a specific entry.
     */
    void evict(String cacheKey);

    /**
     * Build a standardized cache key.
     */
    static String buildKey(String game, String username, String region) {
        return game.toUpperCase() + ":" + username.toLowerCase() + ":" + region.toUpperCase();
    }
}
