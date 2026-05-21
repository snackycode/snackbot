package com.stattracker.infrastructure.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stattracker.domain.model.PlayerStats;
import com.stattracker.domain.port.out.StatsCachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-backed implementation of {@link StatsCachePort}.
 * Serializes {@link PlayerStats} to JSON for storage.
 */
@Component
public class RedisCacheAdapter implements StatsCachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheAdapter.class);
    private static final String KEY_PREFIX = "stats:cache:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RedisCacheAdapter(StringRedisTemplate redis) {
        this.redis = redis;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Optional<PlayerStats> get(String cacheKey) {
        try {
            String json = redis.opsForValue().get(KEY_PREFIX + cacheKey);
            if (json == null) {
                return Optional.empty();
            }
            PlayerStats stats = objectMapper.readValue(json, PlayerStats.class);
            return Optional.of(stats);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached stats for key {}: {}", cacheKey, e.getMessage());
            evict(cacheKey); // evict corrupt entry
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Redis GET failed for key {}: {}", cacheKey, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String cacheKey, PlayerStats stats, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(stats);
            redis.opsForValue().set(KEY_PREFIX + cacheKey, json, ttl);
            log.debug("Cached stats for {} with TTL {}", cacheKey, ttl);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize stats for caching: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Redis SET failed for key {}: {}", cacheKey, e.getMessage());
        }
    }

    @Override
    public void evict(String cacheKey) {
        try {
            redis.delete(KEY_PREFIX + cacheKey);
            log.debug("Evicted cache key: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Redis DELETE failed for key {}: {}", cacheKey, e.getMessage());
        }
    }
}
