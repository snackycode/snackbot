package com.stattracker.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for {@link LinkedAccountDocument}.
 */
@Repository
public interface LinkedAccountMongoRepo extends MongoRepository<LinkedAccountDocument, String> {

    Optional<LinkedAccountDocument> findByDiscordUserIdAndGame(String discordUserId, String game);

    List<LinkedAccountDocument> findAllByDiscordUserId(String discordUserId);

    boolean existsByDiscordUserIdAndGame(String discordUserId, String game);

    long deleteByDiscordUserIdAndGame(String discordUserId, String game);
}
