package com.stattracker;

import com.stattracker.application.service.DailyPostService;
import com.stattracker.domain.model.PlayerStats;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.stattracker")
@EnableScheduling
public class StatTrackerApplication {

    private static final Logger log = LoggerFactory.getLogger(StatTrackerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(StatTrackerApplication.class, args);
    }

    /**
     * Connects the application layer's DailyPostService to the JDA Discord adapter
     * upon boot-up, using the defined callback.
     */
    @Bean
    public CommandLineRunner linkApplicationToDiscord(DailyPostService dailyPostService, JDA jda) {
        return args -> {
            log.info("Linking DailyPostService back to JDA client...");

            dailyPostService.setPostCallback((channelId, discordUserId, stats) -> {
                TextChannel channel = jda.getTextChannelById(channelId);
                if (channel == null) {
                    log.warn("Cannot post daily stats: Discord channel {} not found or inaccessible", channelId);
                    return;
                }

                // Send normalized user statistics in a formatted embed to the channel
                channel.sendMessage("<@" + discordUserId + ">, here are your daily stats for **" + stats.game().getDisplayName() + "**!\n" +
                                     "K/D: " + String.format("%.2f", stats.kdRatio()) + " | Win Rate: " + String.format("%.1f%%", stats.winRate() * 100))
                       .queue(
                               success -> log.debug("Daily stats posted to channel {}", channelId),
                               error -> log.error("Failed to post daily stats to channel {}", channelId, error)
                       );
            });

            log.info("DailyPostService integration complete!");
        };
    }
}
