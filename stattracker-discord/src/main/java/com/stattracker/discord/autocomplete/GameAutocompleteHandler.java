package com.stattracker.discord.autocomplete;

import com.stattracker.domain.model.Game;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides autocomplete suggestions for the "game" and "region" option fields
 * across all slash commands.
 */
@Component
public class GameAutocompleteHandler {

    /**
     * Route the autocomplete event to the correct suggestion provider.
     */
    public void handle(CommandAutoCompleteInteractionEvent event) {
        String focusedOption = event.getFocusedOption().getName();
        String currentInput = event.getFocusedOption().getValue().toLowerCase();

        switch (focusedOption) {
            case "game" -> event.replyChoices(suggestGames(currentInput)).queue();
            case "region" -> event.replyChoices(suggestRegions(event, currentInput)).queue();
            default -> event.replyChoices().queue(); // no suggestions
        }
    }

    /**
     * Fuzzy-match game names against the user's current input.
     */
    private List<Command.Choice> suggestGames(String input) {
        return Arrays.stream(Game.values())
                .filter(g -> g.name().toLowerCase().contains(input) ||
                             g.getDisplayName().toLowerCase().contains(input))
                .map(g -> new Command.Choice(g.getDisplayName(), g.name()))
                .limit(25)
                .collect(Collectors.toList());
    }

    /**
     * Suggest valid regions for the currently selected game.
     * Falls back to all games' regions if no game is selected yet.
     */
    private List<Command.Choice> suggestRegions(CommandAutoCompleteInteractionEvent event,
                                                 String input) {
        // Try to read the "game" option that may already be filled
        String gameStr = event.getOption("game") != null
                ? event.getOption("game").getAsString()
                : null;

        String[] regions;
        if (gameStr != null) {
            try {
                Game game = Game.fromString(gameStr);
                regions = game.getValidRegions();
            } catch (IllegalArgumentException e) {
                regions = allRegions();
            }
        } else {
            regions = allRegions();
        }

        return Arrays.stream(regions)
                .distinct()
                .filter(r -> r.toLowerCase().contains(input))
                .map(r -> new Command.Choice(r, r))
                .limit(25)
                .collect(Collectors.toList());
    }

    private String[] allRegions() {
        return Arrays.stream(Game.values())
                .flatMap(g -> Arrays.stream(g.getValidRegions()))
                .distinct()
                .toArray(String[]::new);
    }
}
