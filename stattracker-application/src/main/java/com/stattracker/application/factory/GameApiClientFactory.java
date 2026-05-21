package com.stattracker.application.factory;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.port.out.GameApiClient;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Routes to the correct {@link GameApiClient} implementation based on the {@link Game} enum.
 * All game-specific API clients are auto-discovered via Spring DI.
 */
@Component
public class GameApiClientFactory {

    private final Map<Game, GameApiClient> clientsByGame;

    /**
     * Spring injects all beans implementing {@link GameApiClient}.
     * Each one declares which game it supports via {@link GameApiClient#supportedGame()}.
     */
    public GameApiClientFactory(List<GameApiClient> clients) {
        this.clientsByGame = new EnumMap<>(Game.class);
        for (GameApiClient client : clients) {
            GameApiClient existing = clientsByGame.put(client.supportedGame(), client);
            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate GameApiClient for " + client.supportedGame() +
                        ": " + existing.getClass().getSimpleName() +
                        " and " + client.getClass().getSimpleName());
            }
        }
    }

    /**
     * @throws IllegalArgumentException if no client is registered for the given game
     */
    public GameApiClient getClient(Game game) {
        GameApiClient client = clientsByGame.get(game);
        if (client == null) {
            throw new IllegalArgumentException("No API client registered for game: " + game);
        }
        return client;
    }

    /**
     * @return true if an API client exists for the given game
     */
    public boolean isSupported(Game game) {
        return clientsByGame.containsKey(game);
    }
}
