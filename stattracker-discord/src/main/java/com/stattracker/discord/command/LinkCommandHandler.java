package com.stattracker.discord.command;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.LinkedAccount;
import com.stattracker.domain.port.in.LinkAccountUseCase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

import java.awt.Color;

/**
 * Handles the {@code /link} slash command — links or unlinks a Discord user's game account.
 */
@Component
public class LinkCommandHandler implements SlashCommandHandler {

    private final LinkAccountUseCase linkUseCase;

    public LinkCommandHandler(LinkAccountUseCase linkUseCase) {
        this.linkUseCase = linkUseCase;
    }

    @Override
    public String getCommandName() {
        return "link";
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue(); // ephemeral

        String discordUserId = event.getUser().getId();
        String gameStr = event.getOption("game", OptionMapping::getAsString);
        String username = event.getOption("username", OptionMapping::getAsString);
        String region = event.getOption("region", OptionMapping::getAsString);

        Game game = Game.fromString(gameStr);

        LinkedAccount linked = linkUseCase.link(discordUserId, game, username, region);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔗 Account Linked!")
                .setColor(Color.GREEN)
                .setDescription("Successfully linked your Discord account to **" +
                        game.getDisplayName() + "**.")
                .addField("In-Game Name", linked.getGameUsername(), true)
                .addField("Region", linked.getRegion(), true)
                .addField("Game", game.getDisplayName(), true)
                .setFooter("Use /stats without a username to look up your linked account");

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }
}
