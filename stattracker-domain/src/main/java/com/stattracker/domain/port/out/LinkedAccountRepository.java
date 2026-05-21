package com.stattracker.domain.port.out;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.LinkedAccount;

import java.util.List;
import java.util.Optional;

/**
 * Secondary (driven) port for persisting linked-account data.
 * Implemented by the infrastructure layer's MongoDB adapter.
 */
public interface LinkedAccountRepository {

    LinkedAccount save(LinkedAccount account);

    Optional<LinkedAccount> findByDiscordUserIdAndGame(String discordUserId, Game game);

    List<LinkedAccount> findAllByDiscordUserId(String discordUserId);

    boolean deleteByDiscordUserIdAndGame(String discordUserId, Game game);

    boolean existsByDiscordUserIdAndGame(String discordUserId, Game game);
}
