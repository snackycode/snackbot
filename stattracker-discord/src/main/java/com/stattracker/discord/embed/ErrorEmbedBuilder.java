package com.stattracker.discord.embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

/**
 * Standardized error embeds for consistent error UX across all commands.
 */
public final class ErrorEmbedBuilder {

    private ErrorEmbedBuilder() {
    }

    public static MessageEmbed rateLimited() {
        return new EmbedBuilder()
                .setTitle("⏳ Slow Down!")
                .setDescription("You're sending commands too fast. Please wait a few seconds and try again.")
                .setColor(Color.ORANGE)
                .build();
    }

    public static MessageEmbed unknownCommand(String commandName) {
        return new EmbedBuilder()
                .setTitle("❓ Unknown Command")
                .setDescription("The command `/" + commandName + "` is not recognized.")
                .setColor(Color.GRAY)
                .build();
    }

    public static MessageEmbed badRequest(String message) {
        return new EmbedBuilder()
                .setTitle("⚠️ Invalid Input")
                .setDescription(message)
                .setColor(Color.YELLOW)
                .build();
    }

    public static MessageEmbed playerNotFound(String username, String game) {
        return new EmbedBuilder()
                .setTitle("🔍 Player Not Found")
                .setDescription("Could not find **" + username + "** on **" + game + "**.\n" +
                        "Double-check the username and region.")
                .setColor(new Color(0xE74C3C))
                .build();
    }

    public static MessageEmbed apiError(String game) {
        return new EmbedBuilder()
                .setTitle("🔌 API Error")
                .setDescription("The **" + game + "** API is currently unavailable. Please try again later.")
                .setColor(new Color(0xE74C3C))
                .build();
    }

    public static MessageEmbed internalError() {
        return new EmbedBuilder()
                .setTitle("💥 Something Went Wrong")
                .setDescription("An unexpected error occurred. The issue has been logged.")
                .setColor(Color.RED)
                .build();
    }

    public static MessageEmbed noLinkedAccount(String game) {
        return new EmbedBuilder()
                .setTitle("🔗 No Linked Account")
                .setDescription("You don't have a linked **" + game + "** account.\n" +
                        "Use `/link` to connect your account.")
                .setColor(new Color(0x3498DB))
                .build();
    }
}
