package com.stattracker.infrastructure.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalTime;

/**
 * MongoDB document for user subscription / premium configuration.
 */
@Data
@NoArgsConstructor
@Document(collection = "subscriptions")
public class SubscriptionDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String discordUserId;

    private String discordChannelId;
    private String tier;           // FREE, PREMIUM, PREMIUM_PLUS
    private boolean dailyPostEnabled;
    private LocalTime dailyPostTime;
    private String dailyPostGame;  // Game enum name
    private Instant createdAt;
    private Instant expiresAt;

    public SubscriptionDocument(String discordUserId, String tier) {
        this.discordUserId = discordUserId;
        this.tier = tier;
        this.dailyPostEnabled = false;
        this.dailyPostTime = LocalTime.of(9, 0);
        this.createdAt = Instant.now();
    }
}
