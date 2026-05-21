package com.stattracker.discord.guard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis-backed per-user rate limiter using a sliding-window counter.
 * Each Discord user is limited to {@link #MAX_REQUESTS} commands within
 * a {@link #WINDOW} period.
 */
@Component
public class UserRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(UserRateLimiter.class);

    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String KEY_PREFIX = "ratelimit:user:";

    private final StringRedisTemplate redis;

    public UserRateLimiter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Attempt to acquire a rate-limit token for the given user.
     *
     * @param userId Discord user snowflake ID
     * @return {@code true} if the request is allowed, {@code false} if rate-limited
     */
    public boolean tryAcquire(String userId) {
        String key = KEY_PREFIX + userId;

        try {
            Long current = redis.opsForValue().increment(key);
            if (current == null) {
                return true;
            }

            if (current == 1L) {
                // First request in this window — set TTL
                redis.expire(key, WINDOW);
            }

            if (current > MAX_REQUESTS) {
                log.debug("User {} exceeded rate limit ({}/{})", userId, current, MAX_REQUESTS);
                return false;
            }

            return true;
        } catch (Exception e) {
            // If Redis is down, fail open (allow the request)
            log.warn("Redis rate-limit check failed — allowing request: {}", e.getMessage());
            return true;
        }
    }

    /**
     * @return remaining requests in the current window, or -1 if unknown
     */
    public int remaining(String userId) {
        String key = KEY_PREFIX + userId;
        try {
            String val = redis.opsForValue().get(key);
            if (val == null) return MAX_REQUESTS;
            return Math.max(0, MAX_REQUESTS - Integer.parseInt(val));
        } catch (Exception e) {
            return -1;
        }
    }
}
