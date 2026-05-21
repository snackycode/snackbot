package com.stattracker.discord.command;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.port.in.CompareUseCase;
import com.stattracker.domain.port.in.CompareUseCase.ComparisonResult;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

import java.awt.Color;

/**
 * Handles the {@code /compare} slash command — compares two players side-by-side.
 */
@Component
public class CompareCommandHandler implements SlashCommandHandler {

    private final CompareUseCase compareUseCase;

    public CompareCommandHandler(CompareUseCase compareUseCase) {
        this.compareUseCase = compareUseCase;
    }

    @Override
    public String getCommandName() {
        return "compare";
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String gameStr = event.getOption("game", OptionMapping::getAsString);
        String player1 = event.getOption("player1", OptionMapping::getAsString);
        String player2 = event.getOption("player2", OptionMapping::getAsString);
        String region = event.getOption("region", "", OptionMapping::getAsString);

        Game game = Game.fromString(gameStr);
        if (region.isBlank()) {
            region = game.getValidRegions()[0];
        }

        ComparisonResult result = compareUseCase.compare(game, player1, region, player2, region);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⚔️ " + player1 + " vs " + player2 + " — " + game.getDisplayName())
                .setColor(new Color(0xF1C40F)) // gold
                .setDescription(result.summary())
                .addField(player1 + " K/D", String.format("%.2f", result.playerA().kdRatio()), true)
                .addField(player2 + " K/D", String.format("%.2f", result.playerB().kdRatio()), true)
                .addBlankField(true)
                .addField(player1 + " Win Rate",
                        String.format("%.1f%%", result.playerA().winRate() * 100), true)
                .addField(player2 + " Win Rate",
                        String.format("%.1f%%", result.playerB().winRate() * 100), true)
                .addBlankField(true)
                .setFooter("Region: " + region);

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }
}
