package com.stattracker.discord.command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;
import java.awt.Color;
import java.time.Instant;
/**
 * A simple activation command (/hello) to verify the connection between
 * the Spring Boot backend and Discord.
 */
@Component
public class HelloCommandHandler implements SlashCommandHandler {
    @Override
    public String getCommandName() {
        return "hello";
    }
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Measure JDA Gateway ping
        long ping = event.getJDA().getGatewayPing();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("👋 Connection Active!")
                .setColor(Color.GREEN)
                .setDescription("Hello! The StatTracker backend is successfully connected to Discord.")
                .addField("Gateway Ping", ping + " ms", true)
                .addField("Backend Status", "🟢 Online & Ready", true)
                .setTimestamp(Instant.now())
                .setFooter("Triggered by " + event.getUser().getAsTag());
        event.replyEmbeds(embed.build()).queue();
    }
}