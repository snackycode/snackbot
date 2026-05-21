package com.stattracker.discord.command;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.MatchSummary;
import com.stattracker.domain.port.in.StatsUseCase;
import com.stattracker.discord.embed.HistoryEmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handles the {@code /history} slash command — shows recent match history.
 */
@Component
public class HistoryCommandHandler implements SlashCommandHandler {

    private static final int DEFAULT_COUNT = 5;

    private final StatsUseCase statsUseCase;
    private final HistoryEmbedBuilder historyEmbedBuilder;

    public HistoryCommandHandler(StatsUseCase statsUseCase,
                                  HistoryEmbedBuilder historyEmbedBuilder) {
        this.statsUseCase = statsUseCase;
        this.historyEmbedBuilder = historyEmbedBuilder;
    }

    @Override
    public String getCommandName() {
        return "history";
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String gameStr = event.getOption("game", OptionMapping::getAsString);
        String username = event.getOption("username", OptionMapping::getAsString);
        int count = event.getOption("count", DEFAULT_COUNT, OptionMapping::getAsInt);

        Game game = Game.fromString(gameStr);
        String region = game.getValidRegions()[0]; // default

        List<MatchSummary> matches = statsUseCase.getRecentMatches(game, username, region, count);

        event.getHook().sendMessageEmbeds(
                historyEmbedBuilder.build(username, game, matches)
        ).queue();
    }
}
