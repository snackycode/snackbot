package com.stattracker.infrastructure.mongo;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.LinkedAccount;
import com.stattracker.domain.port.out.LinkedAccountRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements the domain's {@link LinkedAccountRepository} port
 * using the Spring Data MongoDB {@link LinkedAccountMongoRepo}.
 *
 * <p>Converts between the domain entity ({@link LinkedAccount}) and the
 * persistence document ({@link LinkedAccountDocument}).</p>
 */
@Component
public class LinkedAccountAdapter implements LinkedAccountRepository {

    private final LinkedAccountMongoRepo mongoRepo;

    public LinkedAccountAdapter(LinkedAccountMongoRepo mongoRepo) {
        this.mongoRepo = mongoRepo;
    }

    @Override
    public LinkedAccount save(LinkedAccount account) {
        LinkedAccountDocument doc = toDocument(account);
        LinkedAccountDocument saved = mongoRepo.save(doc);
        return toDomain(saved);
    }

    @Override
    public Optional<LinkedAccount> findByDiscordUserIdAndGame(String discordUserId, Game game) {
        return mongoRepo.findByDiscordUserIdAndGame(discordUserId, game.name())
                .map(this::toDomain);
    }

    @Override
    public List<LinkedAccount> findAllByDiscordUserId(String discordUserId) {
        return mongoRepo.findAllByDiscordUserId(discordUserId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteByDiscordUserIdAndGame(String discordUserId, Game game) {
        long deleted = mongoRepo.deleteByDiscordUserIdAndGame(discordUserId, game.name());
        return deleted > 0;
    }

    @Override
    public boolean existsByDiscordUserIdAndGame(String discordUserId, Game game) {
        return mongoRepo.existsByDiscordUserIdAndGame(discordUserId, game.name());
    }

    // ── Mapping ─────────────────────────────────────────

    private LinkedAccountDocument toDocument(LinkedAccount account) {
        LinkedAccountDocument doc = new LinkedAccountDocument(
                account.getDiscordUserId(),
                account.getGame().name(),
                account.getGameUsername(),
                account.getRegion()
        );
        doc.setId(account.getId());
        doc.setLinkedAt(account.getLinkedAt());
        doc.setVerified(account.isVerified());
        return doc;
    }

    private LinkedAccount toDomain(LinkedAccountDocument doc) {
        LinkedAccount account = new LinkedAccount();
        account.setId(doc.getId());
        account.setDiscordUserId(doc.getDiscordUserId());
        account.setGame(Game.valueOf(doc.getGame()));
        account.setGameUsername(doc.getGameUsername());
        account.setRegion(doc.getRegion());
        account.setLinkedAt(doc.getLinkedAt());
        account.setVerified(doc.isVerified());
        return account;
    }
}
