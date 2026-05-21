package com.stattracker.domain.port.in;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.LinkedAccount;

import java.util.List;
import java.util.Optional;

/**
 * Primary port for linking and unlinking Discord accounts to game accounts.
 */
public interface LinkAccountUseCase {

    /**
     * Link a Discord user's game account.
     *
     * @param discordUserId the Discord snowflake ID
     * @param game          the game being linked
     * @param gameUsername   in-game name
     * @param region        region/platform tag
     * @return the created linked account
     */
    LinkedAccount link(String discordUserId, Game game, String gameUsername, String region);

    /**
     * Remove a previously linked account.
     *
     * @param discordUserId the Discord snowflake ID
     * @param game          the game to unlink
     * @return true if an account was found and removed
     */
    boolean unlink(String discordUserId, Game game);

    /**
     * List all linked accounts for a Discord user.
     */
    List<LinkedAccount> getLinkedAccounts(String discordUserId);

    /**
     * Find the linked account for a specific game, if any.
     */
    Optional<LinkedAccount> getLinkedAccount(String discordUserId, Game game);
}
