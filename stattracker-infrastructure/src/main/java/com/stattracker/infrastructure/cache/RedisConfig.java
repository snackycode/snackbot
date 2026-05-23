package com.stattracker.infrastructure.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    /**
     * Executes a ping on startup to verify the Redis connection factory is configured
     * correctly and the Redis server is reachable.
     */
    @Bean
    public CommandLineRunner verifyRedisConnection(RedisConnectionFactory connectionFactory) {
        return args -> {
            log.info("Verifying connection to Redis...");
            try {
                // Ping the server to ensure we can communicate
                connectionFactory.getConnection().ping();
                log.info("Redis connection established successfully!");
            } catch (Exception e) {
                log.error("Failed to connect to Redis on startup: {}. Please check your Redis instance.", e.getMessage());
            }
        };
    }
}
