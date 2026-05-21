package com.stattracker.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Contract for every slash command handler.
 * Each implementation declares which command name it handles.
 */
public interface SlashCommandHandler {

    /**
     * @return the slash command name this handler responds to (e.g. "stats")
     */
    String getCommandName();

    /**
     * Handle the incoming slash command interaction.
     *
     * @param event the JDA event
     */
    void handle(SlashCommandInteractionEvent event);
}
