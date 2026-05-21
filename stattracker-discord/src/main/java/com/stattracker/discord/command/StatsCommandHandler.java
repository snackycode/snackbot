package com.stattracker.discord.command;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.PlayerStats;
import com.stattracker.domain.port.in.StatsUseCase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

import java.awt.Color;

/**
 * Handles the {@code /stats} slash command — looks up player stats and
 * replies with a rich embed.
 */
@Component
public class StatsCommandHandler implements SlashCommandHandler {

    private final StatsUseCase statsUseCase;

    public StatsCommandHandler(StatsUseCase statsUseCase) {
        this.statsUseCase = statsUseCase;
    }

    @Override
    public String getCommandName() {
        return "stats";
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String gameStr = event.getOption("game", OptionMapping::getAsString);
        String username = event.getOption("username", OptionMapping::getAsString);
        String region = event.getOption("region", "", OptionMapping::getAsString);

        Game game = Game.fromString(gameStr);
        if (region.isBlank()) {
            region = game.getValidRegions()[0]; // default to first valid region
        }

        PlayerStats stats = statsUseCase.getStats(game, username, region);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 " + stats.username() + " — " + game.getDisplayName())
                .setColor(getGameColor(game))
                .addField("Level", String.valueOf(stats.level()), true)
                .addField("K/D Ratio", String.format("%.2f", stats.kdRatio()), true)
                .addField("Win Rate", String.format("%.1f%%", stats.winRate() * 100), true)
                .addField("Total Kills", String.valueOf(stats.totalKills()), true)
                .addField("Total Deaths", String.valueOf(stats.totalDeaths()), true)
                .addField("Total Wins", String.valueOf(stats.totalWins()), true)
                .addField("Matches Played", String.valueOf(stats.totalMatches()), true)
                .setFooter("Region: " + stats.region() + " • Data fetched at " + stats.fetchedAt());

        if (stats.rankedInfo() != null && stats.rankedInfo().isPlaced()) {
            embed.addField("Ranked", stats.rankedInfo().displayString(), false);
        }

        // Add game-specific extra fields
        stats.extra().forEach((key, value) -> embed.addField(key, value, true));

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    private Color getGameColor(Game game) {
        return switch (game) {
            case VALORANT -> new Color(0xFF4654);  // Valorant red
            case LEAGUE -> new Color(0x0BC6E3);    // League teal
            case APEX -> new Color(0xDA292A);       // Apex red
            case FORTNITE -> new Color(0x9D4DBB);  // Fortnite purple
        };
    }
}
