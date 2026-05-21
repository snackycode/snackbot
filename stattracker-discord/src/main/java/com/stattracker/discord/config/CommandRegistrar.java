package com.stattracker.discord.config;

import com.stattracker.discord.command.SlashCommandRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Registers all Discord slash commands globally on application startup
 * and wires up the central event listener.
 */
@Component
public class CommandRegistrar {

    private static final Logger log = LoggerFactory.getLogger(CommandRegistrar.class);

    private final JDA jda;
    private final SlashCommandRegistry registry;

    public CommandRegistrar(JDA jda, SlashCommandRegistry registry) {
        this.jda = jda;
        this.registry = registry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerCommands() {
        log.info("Registering slash commands…");

        jda.updateCommands().addCommands(
                // /stats <game> <username> [region]
                Commands.slash("stats", "Look up a player's stats")
                        .addOptions(
                                new OptionData(OptionType.STRING, "game", "Game to look up", true)
                                        .setAutoComplete(true),
                                new OptionData(OptionType.STRING, "username", "In-game name", true),
                                new OptionData(OptionType.STRING, "region", "Region / platform", false)
                                        .setAutoComplete(true)
                        ),

                // /link <game> <username> <region>
                Commands.slash("link", "Link your game account to Discord")
                        .addOptions(
                                new OptionData(OptionType.STRING, "game", "Game to link", true)
                                        .setAutoComplete(true),
                                new OptionData(OptionType.STRING, "username", "In-game name", true),
                                new OptionData(OptionType.STRING, "region", "Region / platform", true)
                                        .setAutoComplete(true)
                        ),

                // /compare <game> <player1> <player2> [region]
                Commands.slash("compare", "Compare two players' stats")
                        .addOptions(
                                new OptionData(OptionType.STRING, "game", "Game to compare in", true)
                                        .setAutoComplete(true),
                                new OptionData(OptionType.STRING, "player1", "First player's in-game name", true),
                                new OptionData(OptionType.STRING, "player2", "Second player's in-game name", true),
                                new OptionData(OptionType.STRING, "region", "Region (if both share the same)", false)
                                        .setAutoComplete(true)
                        ),

                // /history <game> <username> [count]
                Commands.slash("history", "View recent match history")
                        .addOptions(
                                new OptionData(OptionType.STRING, "game", "Game to look up", true)
                                        .setAutoComplete(true),
                                new OptionData(OptionType.STRING, "username", "In-game name", true),
                                new OptionData(OptionType.INTEGER, "count", "Number of matches (1–20)", false)
                                        .setMinValue(1).setMaxValue(20)
                        ),

                // /hello (activation check)
                Commands.slash("hello", "ankim bot connect to discord")
        ).queue(
                commands -> log.info("Successfully registered {} slash commands", commands.size()),
                error -> log.error("Failed to register slash commands", error)
        );

        // Add the central event listener
        jda.addEventListener(registry);
        log.info("Slash command listener registered");
    }
}
