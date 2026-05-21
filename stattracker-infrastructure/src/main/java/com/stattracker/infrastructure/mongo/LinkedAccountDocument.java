package com.stattracker.infrastructure.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing a linked Discord ↔ game account.
 */
@Data
@NoArgsConstructor
@Document(collection = "linked_accounts")
@CompoundIndex(name = "discord_game_idx", def = "{'discordUserId': 1, 'game': 1}", unique = true)
public class LinkedAccountDocument {

    @Id
    private String id;
    private String discordUserId;
    private String game;        // stored as Game enum name
    private String gameUsername;
    private String region;
    private Instant linkedAt;
    private boolean verified;

    public LinkedAccountDocument(String discordUserId, String game,
                                  String gameUsername, String region) {
        this.discordUserId = discordUserId;
        this.game = game;
        this.gameUsername = gameUsername;
        this.region = region;
        this.linkedAt = Instant.now();
        this.verified = false;
    }
}
