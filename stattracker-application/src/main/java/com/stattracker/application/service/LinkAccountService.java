package com.stattracker.application.service;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.LinkedAccount;
import com.stattracker.domain.port.in.LinkAccountUseCase;
import com.stattracker.domain.port.out.LinkedAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Manages linking and unlinking of Discord accounts to game accounts.
 */
@Service
public class LinkAccountService implements LinkAccountUseCase {

    private static final Logger log = LoggerFactory.getLogger(LinkAccountService.class);

    private final LinkedAccountRepository repository;

    public LinkAccountService(LinkedAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public LinkedAccount link(String discordUserId, Game game, String gameUsername, String region) {
        // Check for existing link
        if (repository.existsByDiscordUserIdAndGame(discordUserId, game)) {
            log.warn("User {} already has a linked {} account — overwriting", discordUserId, game);
            repository.deleteByDiscordUserIdAndGame(discordUserId, game);
        }

        if (!game.isValidRegion(region)) {
            throw new IllegalArgumentException(
                    "Invalid region '" + region + "' for " + game.getDisplayName() +
                    ". Valid regions: " + String.join(", ", game.getValidRegions()));
        }

        LinkedAccount account = new LinkedAccount(discordUserId, game, gameUsername, region);
        LinkedAccount saved = repository.save(account);
        log.info("Linked {} → {}#{} ({})", discordUserId, game, gameUsername, region);
        return saved;
    }

    @Override
    public boolean unlink(String discordUserId, Game game) {
        boolean deleted = repository.deleteByDiscordUserIdAndGame(discordUserId, game);
        if (deleted) {
            log.info("Unlinked {} from {}", discordUserId, game);
        } else {
            log.warn("No {} link found for user {}", game, discordUserId);
        }
        return deleted;
    }

    @Override
    public List<LinkedAccount> getLinkedAccounts(String discordUserId) {
        return repository.findAllByDiscordUserId(discordUserId);
    }

    @Override
    public Optional<LinkedAccount> getLinkedAccount(String discordUserId, Game game) {
        return repository.findByDiscordUserIdAndGame(discordUserId, game);
    }
}
